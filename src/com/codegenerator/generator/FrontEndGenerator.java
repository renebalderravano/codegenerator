package com.codegenerator.generator;

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
		this.packagePath = workspace + "\\" + projectName + "\\src\\app";
		this.resourcesPath = workspace + "\\" + projectName + "\\src\\assets";
		this.server = server;
		this.addOAuth2 = addOAuth2;
	}

	@Override
	public Boolean generate() {
		
		FileManager.createFolder(workspace, projectName);
		FileManager.replaceTextInFilesFolder(workspace + "\\" + projectName,
				"[projectName]", projectName);
		
		for (Object[] table : tables) {
			String tableName = (String) table[0];
			List<Column> columns = jdbcManager.getColumnsByTable(databaseName, tableName);
			Table tbl = new Table();
			tbl.setName(tableName);
			tbl.setColumns(columns);
//			generateModel(tableName, columns);
			generateService(tableName);
		}
		
		return true;
	}

	@Override
	public Boolean generateModel(String tableName, List<Column> columns) {

		return true;
	}

	@Override
	public Boolean generateService(String tableName) {

		String pathService = packagePath + "\\services\\" + formatText(tableName, false) + ".service.ts";
		
		try {
			FileManager.copyDir(PropertiesReading.folder_codegenerator_util + "/FrontEnd/service",					
					workspace + "\\" + formatText(tableName, false), false);
			
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		
		return null;
	}

	@Override
	public Boolean generateComponent(String tableName, List<Column> columns) {
		
		FileManager.copyDir(PropertiesReading.folder_codegenerator_util + "/FrontEnd/component",
				workspace + "\\" + projectName, false);
		return null;
	}
	
	


	private String formatText(String text, boolean capitalize) {
		String[] data = text.split("_");
		if (data.length > 1) {
//			String aux = (capitalize ? data[0].substring(0, 1).toUpperCase() : data[0].substring(0, 1).toLowerCase())
//					+ data[0].substring(1, data[0].length());
//			for (int i = 1; i < data.length; i++) {
//				aux += data[i].substring(0, 1).toUpperCase() + data[i].substring(1, data[i].length());
//			}
//
			text = text.toLowerCase();
		} else {

			text = (capitalize ? text.substring(0, 1).toUpperCase() : text.substring(0, 1).toLowerCase())
					+ text.substring(1, text.length());
		}

		return text;
	}
	
	
	enum TYPEOFCASE{
		CAMEL_CASE,
		SNAKE_CASE
	}
	
	private String formatText(String text, TYPEOFCASE CASE) {
		String[] data = text.split("_");
		
		switch (CASE) {
		case CAMEL_CASE: 
			
			
		break;
		case SNAKE_CASE:
			
			break;
		
		default:
			throw new IllegalArgumentException("Unexpected value: " + CASE);
		}
		
		
		if (data.length > 1) {
//			String aux = (capitalize ? data[0].substring(0, 1).toUpperCase() : data[0].substring(0, 1).toLowerCase())
//					+ data[0].substring(1, data[0].length());
//			for (int i = 1; i < data.length; i++) {
//				aux += data[i].substring(0, 1).toUpperCase() + data[i].substring(1, data[i].length());
//			}
//
			text = text.toLowerCase();
		} else {

//			text = (capitalize ? text.substring(0, 1).toUpperCase() : text.substring(0, 1).toLowerCase())
//					+ text.substring(1, text.length());
		}

		return text;
	}
	
}