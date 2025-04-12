package com.codegenerator.generator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import com.codegenerator.connection.JDBCManager;
import com.codegenerator.util.Column;
import com.codegenerator.util.DataTypeConverter;
import com.codegenerator.util.FileManager;
import com.codegenerator.util.ScriptRunner;
import com.codegenerator.util.Table;

public class BackEndGenerator {

	Set<Object[]> tables;
	JDBCManager jdbcManager;
	String packageName;
	String workspace;
	String projectName;

	String packagePath = "";
	String databaseName;
	String server = "";
	boolean addOAuth2;

	public BackEndGenerator(String server, String databaseName, Set<Object[]> tables, JDBCManager jdbcManager,
			String workspace, String projectName, String packageName, boolean addOAuth2) {
		this.databaseName = databaseName;
		this.tables = tables;
		this.jdbcManager = jdbcManager;
		this.packageName = packageName;
		this.workspace = workspace;
		this.projectName = projectName;
		this.packagePath = workspace + "\\" + projectName + "\\src\\main\\java";
		this.server = server;
		this.addOAuth2 = addOAuth2;
	}

	public boolean generar() {

		System.out.println("Generando...");

		if (databaseName.equals("Seleccione...")) {
			jdbcManager.connect();
			ScriptRunner runner = new ScriptRunner(jdbcManager.getConnection(), addOAuth2, addOAuth2);
			try {
				InputStream is = new FileInputStream("c:/codegenerator/InicioSpringSecurity.sql");
				BufferedReader reader = new BufferedReader(new InputStreamReader(is));
				
				reader = replaceDBName(reader, databaseName);
				runner.runScript(reader);
			} catch (IOException | SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		FileManager.createRootDirectory(workspace, projectName);
		FileManager.createPackage(this.packagePath, this.packageName);
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
			generateRepository(tableName);
			generateService(tableName);
			generateController(tableName);
		}

		FileManager.copyDir("c://codegenerator/util", packagePath + "\\" + packageName.replace(".", "\\") + "\\util",
				false);
		FileManager.copyDir("c://codegenerator/configuration",
				packagePath + "\\" + packageName.replace(".", "\\") + "\\configuration", false);
		FileManager.copyDir("c://codegenerator/pom.xml", workspace + "\\" + projectName + "\\pom.xml", false);

		if (this.addOAuth2)
			FileManager.copyDir("c://codegenerator/UserDetailsServiceImpl.java", packagePath + "\\"
					+ packageName.replace(".", "\\") + "\\service\\impl\\UserDetailsServiceImpl.java", false);
		FileManager.copyDir("c://codegenerator/Application.java",
				packagePath + "\\" + packageName.replace(".", "\\") + "\\Application.java", false);

		FileManager.replaceTextInFilesFolder(packagePath + "\\" + packageName.replace(".", "\\") + "\\util",
				"[packageName]", packageName);
		FileManager.replaceTextInFilesFolder(packagePath + "\\" + packageName.replace(".", "\\") + "\\configuration",
				"[packageName]", packageName);
		FileManager.replaceTextInFile(workspace + "\\" + projectName + "\\pom.xml", "[packageName]", packageName);
		FileManager.replaceTextInFile(workspace + "\\" + projectName + "\\pom.xml", "[projectName]", projectName);
		if (this.addOAuth2)
			FileManager.replaceTextInFile(packagePath + "\\" + packageName.replace(".", "\\")
					+ "\\service\\impl\\UserDetailsServiceImpl.java", "[packageName]", packageName);

		FileManager.replaceTextInFile(packagePath + "\\" + packageName.replace(".", "\\") + "\\Application.java",
				"[packageName]", packageName);

		return true;
	}

	private String getDataTypeJava(String server, String dataType) {
		String dataTypeJava = "";
		if (server.equals("mysql")) {
			dataTypeJava = DataTypeConverter.mysqlToJava.get(dataType);
		} else {
			dataTypeJava = DataTypeConverter.sqlserverToJava.get(dataType);
		}
		return dataTypeJava;
	}

	private String capitalizeText(String text) {
		text = text.toLowerCase().substring(0, 1).toUpperCase() + text.toLowerCase().substring(1, text.length());
		return text;
	}

	public boolean generateModel(String tableName, List<Column> columns) {

		try {

			String pathModel = packagePath + "\\" + packageName.replace(".", "\\") + "\\model\\"
					+ capitalizeText(tableName) + ".java";
			File f = new File(pathModel);
			f.createNewFile();
			Writer w = new OutputStreamWriter(new FileOutputStream(f));

			w.append("package " + packageName + ".model;\n\n");

			w.append("import javax.persistence.*;\n");

			w.append("@Entity\n");
			w.append("@Table(name = \"" + tableName + "\")\n");
			w.append("public class " + capitalizeText(tableName) + "{ \n\n");

			// Add properties
			for (Column column : columns) {

				if (column.getIsPrimaryKey())
					w.append("\t@Id\n");

				w.append("\t@Column(name = \"" + column.getName() + "\")\n");
				w.append("\tprivate " + getDataTypeJava(this.server, column.getDataType()) + " "
						+ column.getName().toLowerCase() + ";\n\n");
			}

			// Add constructor

			w.append("\tpublic " + capitalizeText(tableName) + "(){\n");
			w.append("\t}\n\n");

			// Add method setters and getters

			for (Column column : columns) {

				w.append("\tpublic " + getDataTypeJava(this.server, column.getDataType()) + " get"
						+ capitalizeText(column.getName().toLowerCase()) + "(){\n");
				w.append("\t\treturn " + column.getName().toLowerCase() + ";\n");
				w.append("\t}\n\n");

				w.append("\tpublic void set" + capitalizeText(column.getName().toLowerCase()) + "("
						+ getDataTypeJava(this.server, column.getDataType()) + " " + column.getName().toLowerCase()
						+ "){\n");
				w.append("\t\tthis." + column.getName().toLowerCase() + " = " + column.getName().toLowerCase() + ";\n");
				w.append("\t}\n\n");
			}

			w.append("}");
			w.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;
	}

	public boolean generateRepository(String tableName) {
		try {
			String pathModel = packagePath + "\\" + packageName.replace(".", "\\") + "\\repository\\"
					+ capitalizeText(tableName) + "Repository.java";
			File f = new File(pathModel);
			f.createNewFile();
			Writer w = new OutputStreamWriter(new FileOutputStream(f));

			w.append("package " + packageName + ".repository;\n\n");
			w.append("import " + packageName + ".model." + capitalizeText(tableName) + ";\n");
			w.append("import " + packageName + ".util.IBase;\n\n");
			w.append("public interface " + capitalizeText(tableName) + "Repository extends IBase<"
					+ capitalizeText(tableName) + ">{ \n\n");
			w.append("}");
			w.close();

			pathModel = packagePath + "\\" + packageName.replace(".", "\\") + "\\repository\\impl\\"
					+ capitalizeText(tableName) + "RepositoryImpl.java";
			f = new File(pathModel);
			f.createNewFile();
			w = new OutputStreamWriter(new FileOutputStream(f));

			w.append("package " + packageName + ".repository.impl;\n\n");

			w.append("import org.springframework.stereotype.Repository;\n");
			w.append("import " + packageName + ".model." + capitalizeText(tableName) + ";\n");
			w.append("import " + packageName + ".repository." + capitalizeText(tableName) + "Repository;\n");
			w.append("import " + packageName + ".util.BaseRepository;\n\n");

			w.append("@Repository\n");
			w.append("public class " + capitalizeText(tableName) + "RepositoryImpl extends BaseRepository<"
					+ capitalizeText(tableName) + "> implements " + capitalizeText(tableName) + "Repository { \n\n");

			w.append("}");
			w.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;
	}

	public boolean generateService(String tableName) {

		try {

			String pathModel = packagePath + "\\" + packageName.replace(".", "\\") + "\\service\\"
					+ capitalizeText(tableName) + "Service.java";
			File f = new File(pathModel);
			f.createNewFile();
			Writer w = new OutputStreamWriter(new FileOutputStream(f));

			w.append("package " + packageName + ".service;\n\n");
			w.append("import " + packageName + ".model." + capitalizeText(tableName) + ";\n");
			w.append("import " + packageName + ".util.IBase;\n\n");
			w.append("public interface " + capitalizeText(tableName) + "Service extends IBase<"
					+ capitalizeText(tableName) + ">{ \n\n");

			w.append("}");
			w.close();

			pathModel = packagePath + "\\" + packageName.replace(".", "\\") + "\\service\\impl\\"
					+ capitalizeText(tableName) + "ServiceImpl.java";
			f = new File(pathModel);
			f.createNewFile();
			w = new OutputStreamWriter(new FileOutputStream(f));

			w.append("package " + packageName + ".service.impl;\n\n");

			w.append("import org.springframework.stereotype.Service;\n");
			w.append("import " + packageName + ".model." + capitalizeText(tableName) + ";\n");
			w.append("import " + packageName + ".service." + capitalizeText(tableName) + "Service;\n");
			w.append("import " + packageName + ".util.BaseService;\n\n");

			w.append("@Service\n");
			w.append("public class " + capitalizeText(tableName) + "ServiceImpl extends BaseService<"
					+ capitalizeText(tableName) + "> implements " + capitalizeText(tableName) + "Service { \n\n");
			w.append("}");
			w.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;
	}

	public boolean generateController(String tableName) {

		try {

			String pathModel = packagePath + "\\" + packageName.replace(".", "\\") + "\\controller\\"
					+ capitalizeText(tableName) + "Controller.java";
			File f = new File(pathModel);
			f.createNewFile();
			Writer w = new OutputStreamWriter(new FileOutputStream(f));

			w.append("package " + packageName + ".controller;\n\n");

			w.append("import org.springframework.web.bind.annotation.RequestMapping;\n");
			w.append("import org.springframework.web.bind.annotation.RestController;\n");
			w.append("import " + packageName + ".model." + capitalizeText(tableName) + ";\n");
			w.append("import " + packageName + ".util.BaseController;\n\n");

			w.append("@RestController\n");
			w.append("@RequestMapping(path = \"" + tableName + "\")\n");
			w.append("public class " + capitalizeText(tableName) + "Controller extends BaseController<"
					+ capitalizeText(tableName) + "> { \n\n");
			w.append("}");
			w.close();

		} catch (IOException e) {

			e.printStackTrace();
		}
		return false;
	}

	private void agregarSpringSecurity() {
		FileManager.copyDir("c://codegenerator/configuration",
				packagePath + "\\" + packageName.replace(".", "\\") + "\\configuration\\security", false);

		FileManager.replaceTextInFilesFolder(packagePath + "\\" + packageName.replace(".", "\\") + "\\configuration",
				"[packageName]", packageName);

		// add spring security oauth2 (ss)
		// create tables
		// create model (user, authorities, user_authorities)
		//

		// add models ss
	}

	private BufferedReader replaceDBName(BufferedReader reader, String dataBaseName) {

		String line;
		try {
			line = reader.readLine();

			StringBuffer text = new StringBuffer();

			while (line != null) {
				line.replace("[DataBaseName]", dataBaseName);
				line = reader.readLine();

			}	
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return reader;
	}

}
