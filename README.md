com.powerdata.openpa
====================

Java classes sub-module for Open Source Power Apps and Utilities

The goal of the API is to make model access simple and fast regardless of the back-end being used.

For details please check the [Javadoc](http://powerdata.github.io/com.powerdata.openpa) Reference.

Data source formats
------

OpenPA Models can be loaded from multiple sources.  The primary source we use is the PowerSimulator Model and Case format.

[Download PowerSimulator Model Format Specification](http://powerdata.github.io/com.powerdata.openpa/PowerSimulatorModelFormats.pdf)

[Download PowerSimulator Case Format Specification](http://powerdata.github.io/com.powerdata.openpa/PowerSimulatorCaseFormats.pdf)

Two sample models are available:

[PALCO model (contains switches)](http://powerdata.github.io/com.powerdata.openpa/psmfmtmodels/palco.zip)

[CASCADIA model (contains switches)](http://powerdata.github.io/com.powerdata.openpa/psmfmtmodels/cascadia.zip)

Converters from other formats such as PSS/e and PSLF to the PowerSimulator formats will be available in the future.

PAModel Usage
-----

* Note that OpenPA is currently under heavy development and the following sections will undergo major revisions soon (7 August, 2014) *

A PAModel provides access to the power system network and equipment

To open a model use the *PAModel.Open()* function.  The parameter to this function is a URI that
describes the back-end storage where the data will come from.  For example, to use a PSSE file named
*/tmp/palco.raw* directly:
```java
PAModel eq = PAModel.Open("psseraw:file=/tmp/palco.raw");
```
or to use a directory of psse CSV files named */tmp/testdata/db*:
```java
PAModel eq = PAModel.Open("pssecsv:path=/tmp/testdata/db");
```
This can even be extended to proprietary databases, for example:
```java
PAModel eq = PAModel.Open("pd2cim:db=/tmp/wecc.pddb&inputctx=Ots");
```
opens the model using the PD2 CIM database named */tmp/wecc.pddb*.

The main point is that the code doesn't need to change, only the URI string.
See URI details for the public model parameters below

Once we have the *PAModel* object, *eq* in our examples, we can access lists of equipment.  For example,
to examine every line in the model you could use:
```java
for(Line line : eq.getLines())
{
    // the variable line will be set to the next line in the list
   ... do some work ...
    // at the end of the loop, when I hit the bracket '}' the line variable
    // becomes the next line and control goes back to the top of the loop.
}
```
Some equipment also has it's own lists.  For example, from the Line object we can access the fromBus()
and from the fromBus() we can find all of the breakers needed to isolate the bus (which of course would
isolate the line as well):
```java
Bus frombus = line.getFromBus();
SwitchList switches = frombus.isolate();
for(Switch s : switches)
{
    s.setState(SwitchState.Open);
}
```

Usage Pattern for equipment lists
---------------------------------
Each object retrieved from a list is backed directly by the list.  These are small
trivial objects which make heavy use of the backing list to answer queries and updates.  In general, references to these objects should not persist outside of immediate usage.  Allowing them to be garbage collected is desirable for overall performance and efficiency.

PsseModel is not exactly PSSe
-----------------------------
By now you may have noticed that the model representation is based on PSS/e but is not exactly PSS/e:
* Line.getJ() will not return a negative sign normally supported by PSS/e.  Caller must use Line.getMeteredEnd().
* Include Switch detail as an extension to allow for more accurate representation of a power system when available.
* 3-winding transformers have been converted to an equivalent representation of 3 2-winding transformermers.
* Phase shifters are in a separate list
* Shunts that are identified as part of the Bus or NontransformerBranch record are converted to entries in the ShuntList.
* SwitchedShunt segments are converted into individual entries in the ShuntList.
* Impedances on any branch object are automatically converted to per-unit on a 100 MVA base and bus base voltage when using getR(), getX(), getZ, and getY()
* Methods (getP, setP, getQ, setQ, etc), are defined to allow common attributes to be written to the back end system when supported.


PsseModel URI description
--------------------

###psseraw
Load a PsseModel from a PSS/e raw file.  Currently supports raw file versions 29 and 30.  The implementation
converts the PSS/e file to a set of CSV files as a side effect.  The default behavior is to 
automaticallly remove the temporary CSV files after loading.
If desired, the temporary CSV files can be left behind using the keepcsv parameter.

*Parameters*
* file=/path/to/raw/file
* keepcsv=/path/to/destination/directory

*Example*
```java
PsseModel model = PsseModel.Open("psseraw:file=/tmp/palco.raw&keepcsv=/tmp/palcocsv");
```

###pssecsv
Loads a PsseModel from a set of CSV files based on the PSS/e 30 format.  The temporary CSV files from the *psseraw* URI are generated
in the format appropriate for this model.  CSV files can also be generated by hand or other methods.  Regardless of the PSS/e version 
of the source data, the CSV file parser currently supports version 30 objects and attributes.

*Parameters*
* path=/path/to/csv/files


Utilities
---------
###Psse API Example
A simple example to fetch all the AC branches and display flows generated from case data. [PsseExample](http://powerdata.github.io/com.powerdata.openpa/com/powerdata/openpa/tools/PsseExample.html)

###Sparse B Matrix processing
A sparse B matrix can be generated using class [SparseBMatrix](http://powerdata.github.io/com.powerdata.openpa/com/powerdata/openpa/tools/SparseBMatrix.html).
Call factorize() to return a [FactorizedBMatrix](http://powerdata.github.io/com.powerdata.openpa/com/powerdata/openpa/tools/FactorizedBMatrix.html).
The FactorizedBMatrix class will eventually allow B values to be changed when appropriate without
having to re-eliminate the original matrix.  Call FactorizedBMatrix.solve to run the forward reduction and backward
substitution.

###PowerCalculator

The [PowerCalculator](http://powerdata.github.io/com.powerdata.openpa/com/powerdata/openpa/psse/powerflow/PowerCalculator.html).
 class provides utilities to calculate branch flows and mismatches.

###Fast Decoupled Power Flow
The [FastDecoupledPowerFlow](http://powerdata.github.io/com.powerdata.openpa/com/powerdata/openpa/psse/powerflow/FastDecoupledPowerFlow.html)
class provides services to solve bus angles and voltages for the given model.
The flow can be run from a flat start, from realtime data provided by the OpenPA API,
or from a previous powerflow run.  Use the getVA() and getVM() methods to retrieve
the results.  These can then be used with the PowerCalculator class to generate
flows if desired.

The main() method provides a calling example.

