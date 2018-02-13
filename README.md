# Chordalysis
Learning the structure of graphical models from datasets with thousands of variables
More information about the research papers detailing the theory behind Chordalysis is available at http://www.francois-petitjean.com/Research

# Underlying research and scientific papers

This code is supporting 4 research papers:
* KDD 2016: A multiple test correction for streams and cascades of statistical hypothesis tests
* SDM 2015: Scaling log-linear analysis to datasets with thousands of variables
* ICDM 2014: A statistically efficient and scalable method for log-linear analysis of high-dimensional data
* ICDM 2013: Scaling log-linear analysis to high-dimensional data

When using this repository, please cite:
```
@INPROCEEDINGS{Petitjean2016-KDD,
  author = {Webb, Geoffrey I. and Petitjean, Francois},
  title = {A multiple test correction for streams and cascades of statistical hypothesis tests},
  booktitle = {ACM SIGKDD International Conference on Knowledge Discovery and Data Mining},
  year = 2016,
  pages = {1225--1264}
}

@INPROCEEDINGS{Petitjean2015-SDM,
  author = {Petitjean, Francois and Webb, Geoffrey I.},
  title = {Scaling log-linear analysis to datasets with thousands of variables},
  booktitle = {SIAM International Conference on Data Mining},
  year = 2015,
  pages = {469--477}
}

@INPROCEEDINGS{Petitjean2014-ICDM-1,
  author = {Petitjean, Francois and Allison, Lloyd and Webb, Geoffrey I. and Nicholson, Ann E.},
  title = {A statistically efficient and scalable method for log-linear analysis of high-dimensional data},
  booktitle = {IEEE International Conference on Data Mining},
  year = 2014,
  pages = {480--489}
}

@INPROCEEDINGS{Petitjean2013-ICDM,
  author = {Petitjean, Francois and Webb, Geoffrey I. and Nicholson, Ann E.},
  title = {Scaling log-linear analysis to high-dimensional data},
  booktitle = {IEEE International Conference on Data Mining},
  year = 2013, 
  pages = {597--606}
}
```

# Prerequisites

Chordalysis requires Java 8 (to run) and Ant (to compile); other supporting library are providing in the `lib` folder (with associated licenses). 

# Installing

## Compiling Chordalysis
```
git clone https://github.com/fpetitjean/Chordalysis
cd Chordalyis
ant compile
``` 
## Getting a cross-platform jar and launching the GUI
Simply entering `ant jar` will create a jar file that you can execute in most environments in `bin/jar/Chordalyis.jar`. 
Normal execution would then look like
```java -jar -Xmx1g bin/jar/Chordalysis.jar```
Note that `Xmx1g` means that you are allowing the Java Virtual Machine to use 1GB - althought this is ok for most datasets, please increase if your dataset is large. 

## Running Chordalysis in command line
The `compile` command creates all `.class` files in the `bin/` directory. To execute the demos, simply run:
```
java -Xmx1g -classpath bin:lib/core/commons-math3-3.2.jar:lib/core/jayes.jar:lib/core/jgrapht-jdk1.6.jar:lib/extra/jgraphx.jar:lib/loader/weka.jar demo.RunGUIProof
```
This will run the GUI, which will take you through choosing the different options. 

If you want to run everythin in command line, please run: 
```
java -Xmx1g -classpath bin:lib/core/commons-math3-3.2.jar:lib/core/jayes.jar:lib/core/jgrapht-jdk1.6.jar:lib/extra/jgraphx.jar:lib/loader/weka.jar demo.Run dataFile pvalue imageOutputFile useGUI
```
where:
* `dataFile` represents the path to your dataset in CSV format (eg `/home/doe/mydata.csv`)
* `pvalue` represents the maximum family-wise error rate (FWER); usually `0.05`
* `imageOutputFile` represents the path to the output graph file as an image (eg `/home/doe/mygraph.png`)
* `useGUI` is a boolean used to display the output graph in a GUI or not (eg `false` if you want everything in command line)

There are other demos, allowing you to, for instance, export the probability tables, play with belief propagation, or load a dataset in `.arff` format. Please just contact me if you need help. 

# Chordalysis for R
We now have an R interface for Chordalysis, see:
* CRAN: https://cran.r-project.org/web/packages/ChoR/index.html
* GitHub: https://github.com/HerrmannM/Monash-ChoR

# Support
YourKit is supporting Chordalysis open source project with its full-featured Java Profiler.
YourKit is the creator of innovative and intelligent tools for profiling Java and .NET applications. http://www.yourkit.com 

