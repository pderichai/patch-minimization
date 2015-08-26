package edu.washington.bugisolation;

import java.util.List;

/**
 * An interface that defines the information that the bug minimizer needs to operate.
 * 
 * @author Deric Hua pang
 *
 */
public interface ProjectInfo {

	public static final String D4J_LOCATION = "sh /Users/dpang/workspace/defects4j-github/defects4j.sh";
	public static final String WORKSPACE = "/Users/dpang/workspace/";

	/**
	 * Gets the name of the current project.
	 * 
	 * @return     the name of the current project
	 */
	public abstract String getProjectName();

	/**
	 * Gets the version of the current project.
	 * 
	 * @return     the version number of the current project
	 */
	public abstract String getProjectVersion();
	
	/**
	 * Return whether or not the minimization is on the fixed or buggy version of the project
	 * 
	 * @return     whether or not the minimization process is from buggy to fixed
	 */
	public abstract boolean isFixedToBuggy();
	
	/**
	 * Gets the full name of the current project.
	 * 
	 * @return     the project name and version separated by an underscore
	 */
	public abstract String getFullProjectName();

	/**
	 * Gets the full fixed name of the current project.
	 * 
	 * @return     the full project name followed by "_fixed"
	 */
	public abstract String getFixedName();
	
	/**
	 * Gets the directory of the fixed version of the project.
	 * 
	 * @return     the directory of the fixed version of the project
	 */
	public abstract String getFixedDirectory();

	/**
	 * Gets the full buggy name of the current project.
	 * 
	 * @return	the full project name followed by "_buggy"
	 */
	public abstract String getBuggyName();
	
    /**
     * Gets the directory of the buggy version of the project.
     * 
     * @return     the directory of the buggy version of the project
     */
	public abstract String getBuggyDirectory();
	
	/**
	 * Gets the directory of the version of the project that patches are to be applied.
	 * 
	 * @return     the directory of the version of the project that patches are to be applied
	 */
	public abstract String getRelevantDirectory();
	
	/**
	 * Gets the name of the project that patches are to be applied.
	 * 
	 * @return     either the fixed name or the buggy name of the project, depending
	 *             on the direction of minimization
	 */
	public abstract String getRelevantName();

	/**
	 * Gets the A diff file-path.
	 * 
	 * @return	the A diff file-path
	 */
	public abstract String getDiffPathA();

	/**
	 * Gets the B diff file-path.
	 * 
	 * @return	the B diff file-path
	 */
	public abstract String getDiffPathB();

	/**
	 * Gets the trigger tests for the project.
	 * 
	 * @return	the trigger tests for the project
	 */
	public abstract List<String> getTriggerTests();
	
	/**
	 * Gets the source directory for the project.
	 * 
	 * @return	the source directory of the project
	 */
	public abstract String getSrcDirectory();
	
	/**
	 * Gets the directory containing the test files of the project.
	 * 
	 * @return	the directory containing the test files of the project
	 */
	public abstract String getTestDirectory();

	/**
	 * Gets the fixed version of the modified file for the current project.
	 * 
	 * @return	a List of Strings that represent the modified file
	 */
	public abstract List<String> getFixedFile();

	/**
	 * Gets the buggy version of the modified file for the current project.
	 * 
	 * @return	a List of Strings the represent the modified file
	 */
	public abstract List<String> getBuggyFile();
	
	/**
	 * Gets the modified file that patches are to be applied.
	 * 
	 * @return     a List of Strings the represent the relevant modified file
	 */
	public abstract List<String> getRelevantFile();

	/**
	 * Gets the relevant file's fully qualified name.
	 * 
	 * @return	the fully qualified name of the relevant file
	 */
	public abstract String getRelevantFullyQualifiedName();
	
	/**
	 * Gets the file-path in the src or test directory of the project.
	 * 
	 * @param extension 	the extension of the file
	 * @return				the file-path of the relevant file in the src or test directory
	 */
	public abstract String getRelevantFilePath(String extension);

	/**
	 * Sets the fixed version of the relevant file for the project.
	 * 
	 * @param file	the fixed version of the relevant file
	 */
	public abstract void setFixedFile(List<String> file);

	/**
	 * Sets the buggy version of the relevant file for the project.
	 * 
	 * @param file	the buggy version of the relevant file
	 */
	public abstract void setBuggyFile(List<String> file);

}