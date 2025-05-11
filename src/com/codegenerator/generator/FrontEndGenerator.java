package com.codegenerator.generator;

import java.io.File;
import java.util.List;
import java.util.Set;

import com.codegenerator.connection.JDBCManager;
import com.codegenerator.util.Column;
import com.codegenerator.util.FileManager;
import com.codegenerator.util.PropertiesReading;
import com.codegenerator.util.Table;

public class FrontEndGenerator implements IFrontEndGenerator {
	
	Set<Object[]> tables;
	JDBCManager jdbcManager;
	String packageName;
	String workspace;
	String projectName;
	String packagePath = "";
	String resourcesPath = "";
	String databaseName;
	String server = "";

	boolean addOAuth2;
	
	public FrontEndGenerator(String server, String databaseName, Set<Object[]> tables, JDBCManager jdbcManager,
			String workspace, String projectName, String packageName, boolean addOAuth2) {
		this.databaseName = databaseName;
		this.tables = tables;
		this.jdbcManager = jdbcManager;
		this.packageName = packageName;
		this.workspace = workspace;
		this.projectName = projectName;
		this.packagePath = workspace + "\\" + projectName + "\\src\\main\\java";
		this.resourcesPath = workspace + "\\" + projectName + "\\src\\main\\resources";
		this.server = server;
		this.addOAuth2 = addOAuth2;
	}

	@Override
	public Boolean generate() {
		
		File f = new File(workspace + "\\" + projectName);
		f.mkdir();
		
		FileManager.copyDir(PropertiesReading.folder_codegenerator_util + "/frontend",
				packagePath + "\\" + packageName.replace(".", "\\") + "\\util", false);
		
		
		FileManager.createPackage(this.packagePath, this.packageName);
		FileManager.createPackage(this.packagePath, this.packageName + ".configuration");
		FileManager.createPackage(this.packagePath, this.packageName + ".model");
		FileManager.createPackage(this.packagePath, this.packageName + ".repository.impl");
		FileManager.createPackage(this.packagePath, this.packageName + ".service.impl");
		FileManager.createPackage(this.packagePath, this.packageName + ".controller");

		for (Object[] table : tables) {
			String tableName = (String) table[0];
			List<Column> columns = jdbcManager.getColumnsByTable(databaseName, tableName);
			Table tbl = new Table();
			tbl.setName(tableName);
			tbl.setColumns(columns);
			generateModel(tableName, columns);
			generateService(tableName);
		}

		// Preparar carpteta util
		FileManager.copyDir(PropertiesReading.folder_codegenerator_util + "/util",
				packagePath + "\\" + packageName.replace(".", "\\") + "\\util", false);

		FileManager.replaceTextInFilesFolder(packagePath + "\\" + packageName.replace(".", "\\") + "\\util",
				"[packageName]", packageName);
		return null;
	}

	@Override
	public Boolean generateModel(String tableName, List<Column> columns) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean generateService(String tableName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean generateComponent(String tableName, List<Column> columns) {
		// TODO Auto-generated method stub
		return null;
	}

	
}