package edu.washington.bugisolation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.washington.bugisolation.DDInput.Type;
import edu.washington.bugisolation.util.Operations;
import edu.washington.cs.dericp.diffutils.Diff;
import edu.washington.cs.dericp.diffutils.Hunk;
import edu.washington.cs.dericp.diffutils.UnifiedDiff;

/**
 * The DeltaDebugging class allow the user to utilize a delta debugging algorithm on a
 * List of circumstances.
 * 
 * @author dpang
 *
 */
public class DeltaDebugging {

    public static final int FAIL       = -1;
    public static final int PASS       = +1;
    public static final int UNRESOLVED = 0;
    
	private ProjectInfo projectInfo;
	private Project project;

	public enum Granularity {
	    LINEAR, EXPONENTIAL
	}
	
	/**
	 * Constructs a new DeltaDebugging object.
	 * 
	 * @param proj		a ProjectInfo, contains the information of the current project
	 * @param d4j		a Defects4J, allows DeltaDebugging to execute Defects4J commands
	 * @param git		a Git, allows DeltaDeugging to execute git commands
	 */
    public DeltaDebugging(ProjectInfo projectInfo, Project project) {
    	this.projectInfo = projectInfo;
    	this.project = project;
    }
    
    /**
     * Splits a list in a number of sublists.
     * 
     * @param <T>
     * @param list			a List of Lists that are the subsets
     * @param numSubsets	an int, the number of subsets that the list will be split into
     * @return
     */
    private static <T> List<List<T>> split(List<T> list, int numSubsets) {
        List<List<T>> subSets = new LinkedList<List<T>>();
        int start = 0;

        for (int i = 0; i < numSubsets; i++) {
            List<T> subSet = 
                list.subList(start, start + (list.size() - start) / (numSubsets - i));
            subSets.add(subSet);
            start += subSet.size();
        }

        return subSets;
    }

    private static <T> List<T> minusIndices(List<T> circumstances, int stop1, int stop2) {
        List<T> ret = new ArrayList<T>();
        ret.addAll(circumstances.subList(0, stop1));
        ret.addAll(circumstances.subList(stop2, circumstances.size()));
        return ret;
    }
    
    /**
     * Generic test method.
     * 
     * @param circumstances     a List, a configuration that will be tested
     * @return                  an int, denoting whether or not the configuration passed the test	
     */
    /*@SuppressWarnings("unchecked")
     private int test(DDInput input) {
    	if (input.getKind() == Type.HUNKS) {
    	    return testHunk(input);
    	}
    	if (input.getKind() == Type.LINES) {
    	    return testLines(input);
    	}
    	return UNRESOLVED;
    } */
    
    
    /**
     * Returns whether or not a certain configuration of Deltas pass a test.
     * @param <T>
     * 
     * @param config	a List of Deltas, contains the deltas of a given patch
     * @return			an int, denoting whether or not the configuration passed the test
     */
    private int test(DDInput input) {
    	System.out.println("Testing " + input.getCircumstances().size() + " elements");
    	
    	boolean result = false;
    	
    	if (!input.getCircumstances().isEmpty()) {
    		
	    	// remove the irrelevant hunks
    	    input.removeElements();
    	    
    	    // generate the patch
    	    input.getUnifiedDiff().exportUnifiedDiff(ProjectInfo.WORKSPACE + projectInfo.getFullProjectName() + ".diff");
    	    
	    	result = project.applyPatch() != 0;
    	}
    	
    	/* if the patch is applicable */
    	if (!result) {
    		
    		/* test if the project compiles */
    		result = project.compileModified() != 0;
    		
    		/* if the project compiles */
	    	if (!result) {
	    		
	    		/* run the necessary tests */
	    		project.test();
	    		List<String> failingTests = project.getFailingTests();
	    		
	    		/* there are two different passing conditions depending on which
	    		 * direction the delta debugging algorithm proceeds */
	    		if (projectInfo.isFixedToBuggy()) {
		    		result = !projectInfo.getTriggerTests().equals(failingTests);
	    		} else {
	    			result = !failingTests.isEmpty();
	    		}
	    	}
    	}
    	
    	/* reset the project back to its checkout state */
    	project.reset();
    	System.out.println("Configuration returned " + result);
    	System.out.println();
		return Operations.boolToInt(result);
    }
    
    /**
     * Returns whether or not a certain configuration of Strings pass a test.
     * 
     * @param config	a List of Strings, contains the lines of a given chunk
     * @return			an int, denoting whether or not the configuration passed the test
     */
    /* public int testLines(LinesInput input) {
    	System.out.println("Testing chunk of size " + chunkLines.size());
    	
    	boolean result = false;
    	if (!chunkLines.isEmpty()) {
    		
    		// create the new delta 
    		Delta<String> minDelta;
    		if (originalChunk) {
    			minDelta = new ChangeDelta<String>(new Chunk<String>(chunkStart, chunkLines), otherChunk);
    		} else {
    			minDelta = new ChangeDelta<String>(otherChunk, new Chunk<String>(chunkStart, chunkLines));
    		}
    		
	    	// need the minimized list of deltas 
	    	Patch<String> patch = new Patch<String>();
	    	
	    	for (Delta<String> delta : otherDeltas) {
	    		patch.addDelta(delta);
	    	}
	    	
	    	patch.addDelta(minDelta);
	    	
	    	// generate unified diff 
	    	List<String> diffLines;
	    	 if (projectInfo.isFixedToBuggy()) {
	    		diffLines = DiffUtils.generateUnifiedDiff (
	    				projectInfo.getDiffPathA()
	    				, projectInfo.getDiffPathB()
	    				, projectInfo.getFixedFile()
	    				, patch
	    				, 3);
	    	} else {
	    		diffLines = DiffUtils.generateUnifiedDiff (
	    				projectInfo.getDiffPathA()
	    				, projectInfo.getDiffPathB()
	    				, projectInfo.getBuggyFile()
	    				, patch
	    				, 3);
	    	} 
            diffLines = DiffUtils.generateUnifiedDiff (
                    projectInfo.getDiffPathA()
                    , projectInfo.getDiffPathB()
                    , projectInfo.getModifiedFile()
                    , patch
                    , 3);
            
	    	// save the unified diff 
	    	Operations.linesToFile (
	    			diffLines
	    			, ProjectInfo.WORKSPACE + projectInfo.getFullProjectName() + ".diff");
	    	
	    	// apply the patch 
	    	result = project.applyPatch() != 0;
    	}
    	
    	// if the patch is applicable 
    	if (!result) {
    		
    		// test if the project compiles 
    		result = project.compileModified() != 0;
    		
    		// if the project compiles 
	    	if (!result) {
	    		
	    		// run the necessary tests 
	    		project.test();
	    		List<String> failingTests = project.getFailingTests();
	    		
	    		// there are two different passing conditions depending on which
	    		// direction the delta debugging algorithm proceeds 
	    		if (projectInfo.isFixedToBuggy()) {
		    		result = !projectInfo.getTriggerTests().equals(failingTests);
	    		} else {
	    			result = !failingTests.isEmpty();
	    		}
	    	}
    	}
    	
    	// reset the project back to its checkout state 
    	project.reset();
    	System.out.println("Configuration returned " + result);
    	System.out.println();
		return Operations.boolToInt(result);
    } */
    
    /**
     * Minimizes a list of failing input.
     * @param <T>
     * 
     * @param input	    an input that is to be minimized
     * @return          a List of minimal circumstances that pass test
     */
    public List<Integer> ddmin(DDInput input, Granularity gran) {
        
        System.out.println("***** Running ddmin *****");
        System.out.println("minimizing " + input.getKind());
        System.out.println(input.getCircumstances().size() + " initial elements");
        
        List<Integer> circumstances = input.getCircumstances();
       
        if (input.getKind() == Type.DIFFS) {
            assert test(new DiffsInput(input.getUnifiedDiff(), new ArrayList<Integer>())) == PASS;
        }
        if (input.getKind() == Type.HUNKS) {
            assert test(new HunksInput(input.getUnifiedDiff(), new ArrayList<Integer>(), input.getDiffNumber())) == PASS;
        }
        if (input.getKind() == Type.LINES) {
            assert test(new LinesInput(input.getUnifiedDiff(), new ArrayList<Integer>(), input.getDiffNumber(), input.getHunkNumber())) == PASS;        
        }
        
        assert test(input) == FAIL;
        
        int n = 2;
        
        Map<Integer, Integer> indices = new HashMap<Integer, Integer>();
        
        while (circumstances.size() >= 2) {
            
            List<List<Integer>> subsets = split(circumstances, n);
            assert subsets.size() == n;

            System.out.println("ddmin: testing subsets");

            boolean some_complement_is_failing = false;
            
            loop: for (int i = 0; i < n; i++) {
                         
                int stop1 = i * (circumstances.size() / n);
                int stop2 = stop1 + (circumstances.size() / n);
                
                System.out.println("stop1: " + stop1 + " stop2: " + stop2);
                
                /* checks if the indices have been tested already */
                if (indices.containsKey(stop1) && indices.get(stop1).equals(stop2)) {
                    System.out.println("FOUND REPEATED INDICES");
                    continue loop;
                } else {
                    indices.put(stop1, stop2);
                }
                
                DDInput complement = null;
                
                if (input.getKind() == Type.DIFFS) {
                    complement = new DiffsInput(input.getUnifiedDiff(), minusIndices(circumstances, stop1, stop2));
                }
                if (input.getKind() == Type.HUNKS) {
                    complement = new HunksInput(input.getUnifiedDiff(), minusIndices(circumstances, stop1, stop2), input.getDiffNumber());
                }
                if (input.getKind() == Type.LINES) {
                    complement = new LinesInput(input.getUnifiedDiff(), minusIndices(circumstances, stop1, stop2), input.getDiffNumber(), input.getHunkNumber());
                }

                if (test(complement) == FAIL) {
                    circumstances = complement.getCircumstances();
                    indices.clear();
                    n = Math.max(n - 1, 2);
                    some_complement_is_failing = true;
                    break;
                }
            }

            if (!some_complement_is_failing) {
                if (n == circumstances.size()) {
                    break;
                }

                System.out.println("ddmin: increasing granularity");
                int newGran = circumstances.size();
                if (gran == Granularity.LINEAR) {
                    newGran = ++n;
                }
                if (gran == Granularity.EXPONENTIAL) {
                    newGran = n * 2;
                }
                n = Math.min(newGran, circumstances.size());
                System.out.println("granularity is now " + n);
            }
        }
        
        System.out.println(circumstances.size() + " resulting elements");
        return circumstances;
    }
    
    public UnifiedDiff minimizeDiffs(UnifiedDiff unifiedDiff) {
        System.out.println("minimizing diffs");
        if (unifiedDiff.getDiffs().size() > 1) {
            List<Integer> circumstances = new ArrayList<Integer>();
            for (int i = 0; i < unifiedDiff.getDiffs().size(); ++i) {
                circumstances.add(i);
            }
            
            DDInput minimizedPatch = new DiffsInput(unifiedDiff, circumstances);

            minimizedPatch = new DiffsInput(unifiedDiff, ddmin(minimizedPatch, Granularity.LINEAR));
            
            minimizedPatch.removeElements();
            minimizedPatch.getUnifiedDiff().exportUnifiedDiff(ProjectInfo.WORKSPACE + "defects4j-data/" + projectInfo.getFullProjectName() + "_" + Boolean.toString(projectInfo.isFixedToBuggy()) + "_minDiffs.diff");
            return minimizedPatch.getUnifiedDiff();
        } else {
            System.out.println("only one diff, no need to minimize");
            unifiedDiff.exportUnifiedDiff(ProjectInfo.WORKSPACE + "defects4j-data/" + projectInfo.getFullProjectName() + "_" + Boolean.toString(projectInfo.isFixedToBuggy()) + "_minDiffs.diff");
            return unifiedDiff;
        }
    }
    
    public UnifiedDiff minimizeHunks(UnifiedDiff unifiedDiff) {
        System.out.println("minimizing hunks");
        
        // priming the loop
        DDInput minimizedPatch = new HunksInput(unifiedDiff);
        
        for (int i = 0; i < unifiedDiff.getDiffs().size(); i++) {
            Diff currentDiff = unifiedDiff.getDiffs().get(i);
            // if the current diff has more than one hunk
            if (currentDiff.getHunks().size() > 1) {
                if (currentDiff.getHunks().get(i) != null) {
                    List<Integer> circumstances = new ArrayList<Integer>();
                    for (int j = 0; j < currentDiff.getHunks().size(); ++j) {
                        circumstances.add(j);
                    }
                    minimizedPatch.setCircumstances(circumstances, i, -1);
                    DDInput hunksInput = new HunksInput(unifiedDiff, ddmin(minimizedPatch, Granularity.EXPONENTIAL), i);
                    hunksInput.removeElements();
                    minimizedPatch = hunksInput;
                }
            } else {
                System.out.println("only one hunk, no need to minimize");
            }
        }
        minimizedPatch.getUnifiedDiff().exportUnifiedDiff(ProjectInfo.WORKSPACE + "defects4j-data/" + projectInfo.getFullProjectName() + "_" + Boolean.toString(projectInfo.isFixedToBuggy()) + "_minHunks.diff");
        return minimizedPatch.getUnifiedDiff();
    }
    
    public UnifiedDiff minimizeLines(UnifiedDiff unifiedDiff) {
        System.out.println("minimizing lines");
        
        // priming the loop
        DDInput minimizedPatch = new LinesInput(unifiedDiff);
        
        for (int i = 0; i < unifiedDiff.getDiffs().size(); ++i) {    
            Diff currentDiff = unifiedDiff.getDiffs().get(i);
            
            // j denotes the hunk number
            for (int j = 0; j < currentDiff.getHunks().size(); j++) {
                System.out.println("minimzing diff " + i + " and hunk " + j);
                Hunk currentHunk = currentDiff.getHunks().get(j);
                
                if (currentHunk == null) {
                    System.out.println("this hunk has been remoeved");
                } else {
                    List<Integer> circumstances = new ArrayList<Integer>();
                    for (int k = 0; k < currentHunk.getModifiedLines().size(); ++k) {
                        circumstances.add(k);
                    }
                    minimizedPatch.setCircumstances(circumstances, i, j);
                    DDInput linesInput = new LinesInput(unifiedDiff, ddmin(minimizedPatch, Granularity.LINEAR), i, j);
                    linesInput.removeElements();
                    minimizedPatch = linesInput;
                }
            }
        }
        minimizedPatch.getUnifiedDiff().exportUnifiedDiff(ProjectInfo.WORKSPACE + "defects4j-data/" + projectInfo.getFullProjectName() + "_" + Boolean.toString(projectInfo.isFixedToBuggy()) + "_minLines.diff");
        return minimizedPatch.getUnifiedDiff();
    }
}

