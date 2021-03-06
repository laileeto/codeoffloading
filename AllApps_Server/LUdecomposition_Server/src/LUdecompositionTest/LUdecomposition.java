/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package LUdecompositionTest;

import java.lang.reflect.Method; 
import MC.NetClasses.CloudRemotable;
import java.util.Vector;


import com.cloud.cloudTest.Cloud;

/**
* LuTest
*
* LU decomposition (linear equations).
* These routines come from "Numerical Recipes in Pascal".
* Note that, as in the assignment algorithm, though we
* separately define LUARRAYROWS and LUARRAYCOLS, the two
* must be the same value (this routine depends on a square
* matrix).
*/

public class LUdecomposition
 extends CloudRemotable {

// Notice that we use a 3-D array. This allows us to create
// an array of matrices.

private double[][][] a;     // The matrix itself.
private double [][] b;      // The result vector.
private double [] LUtempvv; // Temporary vector used in ludecomp.
private int [] indx;        // Indexes of permutations.
private int d;              // Indicates even/odd permutations.

/**
* constructor
*
* This routine sets the adjustment flag to false (default indicating
* no adjustment done yet) and initialize the test name (for error
* context reasons). It also loads up important instance variables
* with their "starting" values
*/

public LUdecomposition()
{
    testname = "FPU: LU Decomposition";  // Set test name.

    array_rows = BMglobals.luarrayrows;  // Array dimemsion,default 101.

    base_iters_per_sec = BMglobals.lutestbase; // Score indexing base.

    num_arrays = BMglobals.lunumarrays;  // Starting # arrays.
                                         // Default 1.
    adjust_flag = BMglobals.LUadjust;    // Has test adjusted to clock?
                                         // Default is false at first,
                                         // so adjust.
}


//public static void main(String[] args) {
//
//    System.out.println("\nAverage LUdecomposition Time: " + new LUdecomposition().dotest());
//}

/*
* initialize
*
* Initialize the arrays in preparation for a test.
*/

void initialize()
{
    // Allocate arrays.
    // Note that we create a square matrix, so we just need
    // to know array_rows.

    int i, j;
    a = new double [num_arrays][array_rows][array_rows];
    b = new double [num_arrays][array_rows];
    LUtempvv = new double [array_rows];
    indx = new int [array_rows];

    // Garbage collect.

    System.gc();

    // Initialize the first array.

    build_problem(0);

    // Copy the first array into the other arrays.

    for (i = 1; i < num_arrays; i++)
        for (j = 0; j < array_rows; j++)
        {
            b[i][j] = b[0][j];
            System.arraycopy(a[0][j],0,a[i][j],0,array_rows);
        }

    if (num_arrays > 1)
        for (i = 0; i < array_rows; i++)

    for (j = 0; j < array_rows; j++)
        if (a[0][i][j] != a[1][i][j])
            System.out.println("**LU: Copy error**");
}

/*
* DoIteration
*
* Perform an iteration of the string sort benchmark. This will
* initialize the arrays, perform and time the test. Returns the
* elapsed time in milliseconds.
*/

long DoIteration()
{
    long testTime;      // Duration of the test (milliseconds).

    // Initialization not necessary...done elsewhere.

    // Start the stopwatch.

    testTime = System.currentTimeMillis();

    // Step through each of the LU arrays. Perform decomp
    // on each as you go through.

    for (int i = 0; i < num_arrays; i++)
        lusolve(i);

    testTime = System.currentTimeMillis() - testTime;

    return(testTime);
}

/**
* dotest
*
* Perform the actual string benchmark test.
* The steps involved are:
*  1. See if the test has already been "adjusted".
*     If it hasn't do an adjustment phase.
*  2. If the test has been adjusted, go ahead and
*      run it.
*/

@Cloud
public long localdotest()
{
    long duration;          // Time in milliseconds for doing test.

    // Has it been adjusted yet?

    if(adjust_flag == false)
    {

        // Do adjustment phase

        while (true)
        {
            // Initialize.
            initialize();

            // See if the current setting meets requirements.

            duration = DoIteration();
            if (duration > BMglobals.minTicks / 10) // minTicks = 100 ticks.
                break;                              // We'll take just 10.

            // Current setting does not meet requirements.
            // Increse # of arrays and try again.

            num_arrays += 1;
        }

        // Scale up to whatever it takes to do 100 clock ticks.

        int factor = (int) (BMglobals.minTicks / duration);
        num_arrays += (factor * num_arrays);

        adjust_flag = true;                // Don't adjust next time.

    }   // End adjust section.

    // (If we fall though preceding IF statement, adjustment
    //  not necessary -- simply initialize.

    initialize();

    // All's well if we get here. Perform the test.

    duration = DoIteration();

    // Calculate the iterations per second. Note that we
    // assume here that duration is in milliseconds.

    iters_per_sec = (double)num_arrays / ((double)duration /
        (double)1000);

    // Debug code: Checks that first array (TestArray[0]) is sorted.

    if (debug_on == true)
    {
        System.out.println("--- LU Decomposition debug data ---");
        System.out.println("Number of arrays: " + num_arrays);
        System.out.println("Elapsed time: " + duration);
        System.out.println("Iterations per sec: " + iters_per_sec);

        // Print out first ten b[] of first array.

        System.out.println("Solved first 10 b[]");
        for(int i = 0; i < 10; i++)
        {
            System.out.print(b[0][i]);
            System.out.print(" ");
        }

        System.out.println("<");
    }

    // Clean up and exit.

    cleanup();

    return duration;
}

/*
* cleanup
*
* Clean up after the LU benchmark. This simply points the
* arrays at empty arrays and performs system garbage collection.
*/

void cleanup()
{
    a = null;
    b = null;
    LUtempvv = null;
    indx = null;

    System.gc();                // Garbage collect it.
}


/*
* build_problem
*
* Constructs a solvable set of linear equations. It does this by
* creating an identity matrix, then loading the solution vector
* with random numbers. After that, the identity matrix and
* solution vector are randomly "scrambled."  Scrambling is
* done by (a) randomly selecting a row and multiplying that
* row by a random number and (b) adding one randomly-selected
* row to another.
*/
private void build_problem(int anum)
{

int i, j, k, k1;      // Indexes.
RandNum rndnum = new RandNum();   // Random number generator.
double rcon;                      // Randomly-generated constant (?)

// Build an identity matrix. We'll also use this as a chance to
// load the solution vector.

for (i = 0; i < array_rows; i++)
{
    b[anum][i] = (double) (rndnum.abs_nextwc(100) + 1);

	for (j = 0; j < array_rows; j++)
		if (i == j)
		    a[anum][i][j] = (double) (rndnum.abs_nextwc(1000) + 1);
		else
			a[anum][i][j] = (double)0.0;
}

// Scramble. Do this 8n times. See comment above for a description
// of the scrambling process.

for (i = 0; i < 8 * array_rows; i++)
{
	// Pick a row and a random constant. Multiply all elements
	// in the row by the constant.

/*  k = rndnum.abs_nextwc(array_rows);
	rcon = (double) (rndnum.abs_randwc(20) + 1);
	for(j = 0; j < array_rows; j++)
		a[anum][k][j] = a[anum][k][j] * rcon;
	b[anum][k] = b[anum][k] * rcon;
*/
	// Pick two random rows and add second to first. Note that
	// we also occasionally multiply by minus 1 so that we get
	// a subtraction operation.

	k = rndnum.abs_nextwc(array_rows);
	k1 = rndnum.abs_nextwc(array_rows);
	if (k != k1)
	{
		if (k < k1)
		    rcon = (double) 1.0;
		else
		    rcon = (double) -1.0;

		for (j = 0; j < array_rows; j++)
			a[anum][k][j] += a[anum][k1][j] * rcon;

		b[anum][k] += b[anum][k1] * rcon;
	}
}

return;
}

/*
* ludcmp
*
* From the procedure of the same name in "Numerical Recipes in Pascal",
* by Press, Flannery, Tukolsky, and Vetterling.
* Given an n x n matrix a[][], this routine replaces it by the LU
* decomposition of a row-wise permutation of itself. a[] and n
* are input. a[][] is output, modified as follows:
*   --                       --
*  |  b(1,1) b(1,2) b(1,3)...  |
*  |  a(2,1) b(2,2) b(2,3)...  |
*  |  a(3,1) a(3,2) b(3,3)...  |
*  |  a(4,1) a(4,2) a(4,3)...  |
*  |  ...                      |
*   --                        --
*
* Where the b(i,j) elements form the upper triangular matrix of the
* LU decomposition, and the a(i,j) elements form the lower triangular
* elements. The LU decomposition is calculated so that we don't
* need to store the a(i,i) elements, which would have laid along the
* diagonal and would have all been 1.
*
* indx[] is a vector that records the row permutation effected by
* the partial pivoting; d is modified as +/-1 depending on whether
* the number of row interchanges was even or odd, respectively.
* Returns 0 if matrix singular, else returns 1. anum selects which
* array will be decomposed.
*/

private int ludcmp(int anum)
{

double big;     // Holds largest element value.
double sum;
double dum;     // Holds dummy value.
int i,j,k;      // Indexes.
int imax;       // Holds max index value.
double tiny;    // A really small number.

// Set our really small number to something really small.

tiny = (double) 1.0e-20;

d = 1;            // No interchanges yet.
imax = 0;         // No max index yet.

for (i = 0; i < array_rows; i++)
{
    big = (double)0.0;
	for (j = 0; j < array_rows; j++)
		if (Math.abs(a[anum][i][j]) > big)
			big = Math.abs(a[anum][i][j]);

	// Bail out on singular matrix.

	if (big == (double) 0.0) return(0);

	LUtempvv[i] = 1.0/big;
}

// Crout's algorithm...loop over columns.

for (j = 0; j < array_rows; j++)
{
    if (j != 0)
		for (i = 0; i < j; i++)
		{
		    sum = a[anum][i][j];
			if (i != 0)
				for (k = 0; k < i; k++)
					sum -= (a[anum][i][k] * a[anum][k][j]);
			a[anum][i][j] = sum;
		}

	big = (double) 0.0;
	for (i = j; i < array_rows; i++)
	{
	    sum = a[anum][i][j];
		if (j != 0)
			for (k = 0; k < j; k++)
				sum -= a[anum][i][k] * a[anum][k][j];

		a[anum][i][j] = sum;
		dum = LUtempvv[i] * Math.abs(sum);
		if(dum >= big)
		{
		    big = dum;
			imax = i;
		}
	}

	if (j != imax)             // Interchange rows if necessary.
	{
	    for (k = 0; k < array_rows; k++)
		{
		    dum = a[anum][imax][k];
			a[anum][imax][k] = a[anum][j][k];
			a[anum][j][k] = dum;
		}
        d = -d;               // Change parity of d.

// 		dum = LUtempvv[imax];

		LUtempvv[imax] = LUtempvv[j]; // Don't forget scale factor.

//		LUtempvv[j] = dum;
	}

	indx[j] = imax;

	// If the pivot element is zero, the matrix is singular
	// (at least as far as the precision of the machine
	// is concerned.)  We'll take the original author's
	// recommendation and replace 0.0 with "tiny".

	if (a[anum][j][j] == (double) 0.0)
		a[anum][j][j] = tiny;

	if (j != (array_rows - 1))
	{       dum = 1.0 / a[anum][j][j];
		for (i = j+1; i < array_rows; i++)
			a[anum][i][j] = a[anum][i][j] * dum;
	}
}

return (1);
}

/*
* lubksb
*
* Also from "Numerical Recipes in Pascal".
* This routine solves the set of n linear equations A x = B.
* Here, a[][] is input, not as the matrix A, but as its
* LU decomposition, created by the routine ludcmp().
* Indx[] is input as the permutation vector returned by ludcmp().
* b[] is input as the right-hand side an returns the
* solution vector X.
* a[], n, and indx are not modified by this routine and
* can be left in place for different values of b[].
* This routine takes into account the possibility that b will
* begin with many zero elements, so it is efficient for use in
* matrix inversion.
*/

private void lubksb(int anum)  // anum is array number to be processed.
{

int ii;     // Index.
int i;      // Another index,
int j;      // and another.
int ip;     // Pointer into indx[] for unscrambling the permutations.
double sum;

// When ii is set to a positive value, it will become the index of
// the first nonvanishing element of b[]. We now do the forward
// substitution. The only wrinkle is to unscramble the permutation
// as we go.

ii = -1;
for (i = 0; i < array_rows; i++)
{
    ip = indx[i];
    sum = b[anum][ip];
    b[anum][ip] = b[anum][i];
    if (ii != -1)
        for (j = ii; j < i; j++)
            sum = sum - a[anum][i][j] * b[anum][j];
    else
        // If a nonzero element is encountered, we have to
        //  do the sums in the loop above.

        if(sum != (double) 0.0)
            ii = i;

    b[anum][i] = sum;
}

// Now do back substitution.

for (i = (array_rows - 1); i >= 0; i--)
{
    sum = b[anum][i];
    if(i != (array_rows - 1))
        for(j = ( i + 1); j < array_rows; j++)
            sum = sum - a[anum][i][j] * b[anum][j];
    b[anum][i] = sum / a[anum][i][i];
}

}

/*
* lusolve
*
* Solve a linear set of equations A x = b.
* Original matrix A will be destroyed by this operation
* Returns 0 if matrix is singular, 1 otherwise.
*/

private int lusolve(int anum) // anum selects array number being solved.

{

int i;

// Do LU decomposition. If it returns 0, we've got a singular
//  matrix. Otherwise, we can do back-solving.

if (ludcmp(anum) == 0) return(0);

lubksb(anum);

return(1);
}



boolean adjust_flag; // Initially set false. Set to true
                                  // if the adjustment phase has
                                  // has already occurred on this
                                  // object.
double iters_per_sec = 0;   // Iterations per second. This is
                        // the calculated result of the
                        // current test.

double base_iters_per_sec = 0;  // Baseline iterations per sec. Used
                            // to calculate the index and based
                            // on some standard system.

int numtries = 0;           // # of tries attempted.

double scores[] =       // Array of five scores for confidence
   {0,0,0,0,0};         //  interval testing.

double mean = 0;            // Mean of the scores.

double stdev = 0;           // Standard deviation of scores.

int num_arrays = 0;         // # of arrays used in a test.

int array_rows = 0;         // Size of array in rows. This is also
                        // the variable used to indicate array
                        // size for 1D array tests.

int array_cols = 0;         // Size of array in columns.

int loops_per_iter = 0;         // Some tests increase workload by
                        // adding loops. This holds the # of
                        // loops used in the test.

String testname = "";        // Name of current test.

// Flag to run test in debug mode

boolean debug_on = false;

// Flag to indicate successful test.

boolean testConfidence = true;

/**
* dotest (debug version)
*
* Do the test with debug error checking. This is an overload of the
* dotest() method and is usually the same code from test to test so
* abstract isn't appropriate.
*/

void dotest (boolean debug_on)
{
    this.debug_on = debug_on;
    dotest();
    this.debug_on = false;
}
/**
* benchWithConfidence
*
* Given a reference to a BmarkTest object, this routine
* repeatedly executes its associated benchmark, seeking
* to collect enough scores to get 5 that meet the
* confidence criteria. Return true if OK, else return false.
*/

void benchWithConfidence()
{

double halfinterval;    // Confidence half interval.

// Get first 5 scores. Then begin confidence testing.

for (int i = 0; i < 5; i++)
{
    try
    {
        dotest();        // Run once.
    }
    catch (OutOfMemoryError e)  // Handle test errors/exceptions.
    {
        testConfidence = false; // Flag for bad test.
        throw new OutOfMemoryError(testname + ". Error allocating"
            + " memory for test data.");
    }
}

numtries = 5;                       // Show 5 tries already.

// The system allows a maximum of 10 tries before it
// gives up. Since we've done 5 already, we'll allow
// 5 more.

// Enter loop to test for confidence criteria

while(true)
{
    // Calculate confidence

    halfinterval = calcConfidence();

    // Is half interval 5% or less of mean?
    // If so, we can go home. Otherwise, we
    // have to continue.

    if(halfinterval / mean <= (double).05)
        break;

    // Go get a new score and see if it improves
    // the existing scores.

    do
    {
        if (numtries == 10)
        {
            testConfidence = false;
            return;
        }

        // Run the test

        try
        {
            dotest();               // Run test.
        }
        catch (OutOfMemoryError e)  // Handle test errors/exceptions.
        {
            testConfidence = false; // Flag for bad test.
            throw new OutOfMemoryError(testname + ": Error allocating"
                + " memory for test data.");
        }

        numtries++;

    } while (!seekConfidence());
}

testConfidence = true;
}


/**
* seekConfidence
*
* Given an array of 5 scores PLUS a new score, this routine tries
* the new score in place of each of the other 5 to determine if
* the new score, when replacing one of the others, improves the
* confidence half-interval.
* Return FALSE if failure...original 5 scores unchanged.
* Also calculates new half-interval, mean, and std. deviation.
*/

private boolean seekConfidence()
{

double halfinterval;    // Confidence halfinterval
double stdev_to_beat;   // Standard deviation to be improved
double temp;            // Holding place for score being swapped out
int isbeaten;           // Flag indicating progress; also used to
                        //  hold index of swapped score.

// Calculate original standard deviation.

halfinterval = calcConfidence();
stdev_to_beat = stdev;
isbeaten = -1;

// Try to beat original score by exchanging new score
// with each of the originals one at a time.

for (int i = 0; i < 5; i++)
{
    temp=scores[i];
    scores[i] = iters_per_sec;
    calcConfidence();
    if(stdev_to_beat > stdev)
    {   isbeaten = i;
        stdev_to_beat = stdev;
    }
}

if (isbeaten != -1)
{
    scores[isbeaten] = iters_per_sec;
    return(false);
}

return(true);

}

/**
* calcConfidence
*
* Given a set of 5 scores, calculate the confidence half-interval.
* We'll also calculate the sample mean and sample standard deviation.
* Returns the half-interval. NOTE: This routine presumes a confidence
* of 95% and a confidence coefficient of .95.
*/

private double calcConfidence()
{
double halfinterval;    // Returned half interval value.

// Calculate mean

mean = (scores[0] + scores[1] + scores[2] + scores[3] + scores[4])
       / (double)5;

// Calculate standard deviation -- first get variance.

stdev = (double)0;
for (int i = 0; i < 5; i++)
    stdev += (scores[i] - mean) * (scores[i] - mean);
stdev /= (double) 4;
stdev = Math.sqrt(stdev / (double) 5);

// Now calculate the confidence half-interval. For a confidence
// level of 95% our confidence coefficient gives us a mulitplying
// factor of 2.776. (The upper .025 quartile of a t distribution
// with 4 degrees of freedom.)

halfinterval = stdev * (double) 2.776;
return (halfinterval);
}


public long dotest()
{
	Method toExecute; 
	Class<?>[] paramTypes = null; 
	Object[] paramValues = null; 
	Long result = null;
	try{
		toExecute = this.getClass().getDeclaredMethod("localdotest", paramTypes);
		Vector results = getCloudController().execute(toExecute,paramValues,this,this.getClass());
		if(results != null){
			result = (Long)results.get(0);
			copyState(results.get(1));
		}else{
			result = localdotest();
		}
	}  catch (SecurityException se){
	} catch (NoSuchMethodException ns){
	}catch (Throwable th){
	}
	
	return result;
	}

void copyState(Object state){
	LUdecomposition
 localstate = (LUdecomposition
) state;
	this.a = localstate.a;
	this.b = localstate.b;
	this.LUtempvv = localstate.LUtempvv;
	this.indx = localstate.indx;
	this.d = localstate.d;
	this.adjust_flag = localstate.adjust_flag;
	this.iters_per_sec = localstate.iters_per_sec;
	this.base_iters_per_sec = localstate.base_iters_per_sec;
	this.numtries = localstate.numtries;
	this.scores = localstate.scores;
	this.mean = localstate.mean;
	this.stdev = localstate.stdev;
	this.num_arrays = localstate.num_arrays;
	this.array_rows = localstate.array_rows;
	this.array_cols = localstate.array_cols;
	this.loops_per_iter = localstate.loops_per_iter;
	this.testname = localstate.testname;
	this.debug_on = localstate.debug_on;
	this.testConfidence = localstate.testConfidence;
}
}
