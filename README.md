# RPG
Random Polygon Generator

<pre>

Args can be:

	-n int : to set the number of points.

	-r int : to set the range of the points generated(from -r to r).

	-UI : to use the user interface.

	-img : to create images of the solutions of all the different algorithms.

	-s int : to change the scale of the output, smaller int allows to view more of the graph(zoom out),
	         larger int makes you view less(zoom in).

	-nc : to not generate 3 or more co-linear points. If this flag is not enabled that co-linear points are allowed.
	      Enabling this flag makes the point generation harder.

	-hc [<Heuristic> <FirstCandidate>] : to chose the hill climbing algorithm than you want,
	                                     if -hc and -sa is not present than all the algorithms are
	                                     executed.
	                                     <Heuristic> can be:
	                                         "BI" - Best Improvement(chooses the candidate that improves the perimeter the most).
	                                         "FI" - Fisrt Improvement(chooses the first candidate).
	                                         "LC" - Less conflicts(chooses the candidate that has the least conflicts).
	                                         "R" - Random(chose a random candidate).
	                                     <FirstCandidate> can be:
	                                   	  "NF" - Nearest first(order the points of the first candidate by distance from each other).
	                                   	  "R" - Random(order the points of the first candidate randomly).

	                                     Note: To execute different combinations do : -hc <Heuristic> <FirstCandidate> <Heuristic> <FirstCandidate> ...
	                                   	   example: -hc BI NF BI R FI NF -> executes Best Improvement with Nearest First, Best Improvement with Random and First Improvement with Nearest First.

	-sa [<FirstCandidate>] : to chose simulated annealing algorithm that you want,
	                         as refereed above if -sa or -hc are not present than
	                         all the algorithms are executed.
	                         <FirstCandidate> is the same as in -hc.

	-o <name> : outputs to the file <name>. If the file doesn't exists is created, if it exists than all the contents are erased.

	-p : if this flag is enabled that the output graph will have the points marked.

	-f : if this flag is enabled that the polygon in the output graph will be colored.

	-i <int> : to execute the algorithm <int> times, the output will be the average of all runs.

	Note: the arguments can't be together with flag ex: -n30

Important: If the generation of points is taking too long then you have to lower n or increase r.

</pre>
# How to run

First cd into the root directory.

Then Compile the files 
```console
javac -d bin/ -cp src src/main/Start.java
```
and
```console
java -cp bin main.Start <Args>
```

# Examples
Runs all the algorithm starting with 200 random points, 15 range, 20 scale, and outputs an image of the polygon colored with the points marked.
```console
java -cp bin main.Start -n 200 -r 15 -s 20 -img -hc BI R FI R LC R R R -sa R -p -f
```

Runs all the algorithm starting with 1000 ordered according to nearest first, 15 range, 20 scale, and outputs an image of the polygon colored with the points marked.           
```console
java -cp bin main.Start -n 1000 -r 20 -s 15 -img -hc BI NF FI NF LC NF R NF -sa NF -p -f
```

