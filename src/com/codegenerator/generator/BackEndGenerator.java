package com.codegenerator.generator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import com.codegenerator.connection.JDBCManager;
import com.codegenerator.util.Column;
import com.codegenerator.util.DataTypeConverter;
import com.codegenerator.util.FileManager;
import com.codegenerator.util.PropertiesReading;
import com.codegenerator.util.ScriptRunner;
import com.codegenerator.util.Table;

public class BackEndGenerator {

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

	public BackEndGenerator(String server, String databaseName, Set<Object[]> tables, JDBCManager jdbcManager,
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

	public boolean generar() {

		System.out.println("Generando...");
		try {

			FileManager.createRootDirectory(workspace, projectName);
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
				generateRepository(tableName);
				generateService(tableName);
				generateController(tableName);
			}

			// Preparar carpteta util
			FileManager.copyDir(PropertiesReading.folder_codegenerator_util + "/util",
					packagePath + "\\" + packageName.replace(".", "\\") + "\\util", false);

			FileManager.replaceTextInFilesFolder(packagePath + "\\" + packageName.replace(".", "\\") + "\\util",
					"[packageName]", packageName);

			// Preparar clase principal de spring Boot Application.java
			FileManager.copyDir(PropertiesReading.folder_codegenerator_util + "/Application.java",
					packagePath + "\\" + packageName.replace(".", "\\") + "\\Application.java", false);

			FileManager.replaceTextInFile(packagePath + "\\" + packageName.replace(".", "\\") + "\\Application.java",
					"[packageName]", packageName);

			// preparar configuracion Hibernate

			FileManager.copyDir(
					PropertiesReading.folder_codegenerator_util + "/configuration/HibernateConfiguration.java",
					packagePath + "\\" + packageName.replace(".", "\\")
							+ "\\configuration\\HibernateConfiguration.java",
					false);

			FileManager.copyDir(PropertiesReading.folder_codegenerator_util + "/configuration/WebConfiguration.java",
					packagePath + "\\" + packageName.replace(".", "\\") + "\\configuration\\WebConfiguration.java",
					false);

			FileManager.replaceTextInFilesFolder(
					packagePath + "\\" + packageName.replace(".", "\\") + "\\configuration", "[packageName]",
					packageName);

			if (this.addOAuth2) {

				FileManager.copyDir(PropertiesReading.folder_codegenerator_util + "/security/pom.xml",
						workspace + "\\" + projectName + "\\pom.xml", false);

				FileManager.copyDir(PropertiesReading.folder_codegenerator_util + "/security/configuration",
						packagePath + "\\" + packageName.replace(".", "\\") + "\\configuration", false);

				FileManager.replaceTextInFilesFolder(
						packagePath + "\\" + packageName.replace(".", "\\") + "\\configuration", "[packageName]",
						packageName);

				addTablesSpringSecurity(databaseName);

				FileManager.copyDir(PropertiesReading.folder_codegenerator_util + "/security/model",
						packagePath + "\\" + packageName.replace(".", "\\") + "\\model", false);

				FileManager.replaceTextInFilesFolder(packagePath + "\\" + packageName.replace(".", "\\") + "\\model",
						"[packageName]", packageName);

				FileManager.copyDir(
						PropertiesReading.folder_codegenerator_util + "/security/repository/UserRepository.java",
						packagePath + "\\" + packageName.replace(".", "\\") + "\\repository\\UserRepository.java",
						false);

				FileManager.replaceTextInFilesFolder(
						packagePath + "\\" + packageName.replace(".", "\\") + "\\repository\\UserRepository.java",
						"[packageName]", packageName);

				FileManager.copyDir(
						PropertiesReading.folder_codegenerator_util
								+ "/security/repository/impl/UserRepositoryImpl.java",
						packagePath + "\\" + packageName.replace(".", "\\")
								+ "\\repository\\impl\\UserRepositoryImpl.java",
						false);

				FileManager.replaceTextInFilesFolder(packagePath + "\\" + packageName.replace(".", "\\")
						+ "\\repository\\impl\\UserRepositoryImpl.java", "[packageName]", packageName);

				generateService("User");

				generateRepository("Authority");
				generateService("Authority");

				FileManager.copyDir(
						PropertiesReading.folder_codegenerator_util + "/UserDetailsServiceImpl.java", packagePath + "\\"
								+ packageName.replace(".", "\\") + "\\service\\impl\\UserDetailsServiceImpl.java",
						false);

				FileManager.replaceTextInFile(packagePath + "\\" + packageName.replace(".", "\\")
						+ "\\service\\impl\\UserDetailsServiceImpl.java", "[packageName]", packageName);
			} else
				FileManager.copyDir(PropertiesReading.folder_codegenerator_util + "/pom.xml",
						workspace + "\\" + projectName + "\\pom.xml", false);

			// preparar archivo pom.xml
			FileManager.replaceTextInFile(workspace + "\\" + projectName + "\\pom.xml", "[packageName]", packageName);
			FileManager.replaceTextInFile(workspace + "\\" + projectName + "\\pom.xml", "[projectName]", projectName);

			FileManager.replaceTextInFile(workspace + "\\" + projectName + "\\pom.xml", "[DBgroupId]",
					PropertiesReading.getProperty(jdbcManager.getServer() + ".groupId"));
			FileManager.replaceTextInFile(workspace + "\\" + projectName + "\\pom.xml", "[DBartifactId]",
					PropertiesReading.getProperty(jdbcManager.getServer() + ".artifactId"));
			FileManager.replaceTextInFile(workspace + "\\" + projectName + "\\pom.xml", "[DBversion]",
					PropertiesReading.getProperty(jdbcManager.getServer() + ".version"));

			// preparar archivo application.properties
			FileManager.copyDir(PropertiesReading.folder_codegenerator_util + "/resources", resourcesPath, false);

			String url = "jdbc:";
			String prop = jdbcManager.getServer().trim() + ".datasource.driver-class-name";
			String driver = PropertiesReading.getProperty(prop);
			StringBuilder urlDB = new StringBuilder(
					PropertiesReading.getProperty(jdbcManager.getServer() + ".datasource.url.databasename"));
			url = urlDB.toString().replace("?1", jdbcManager.getHost()).replace("?2", jdbcManager.getPort())
					.replace("?3", databaseName);

			FileManager.replaceTextInFile(resourcesPath + "\\application.properties", "[driver]", driver);

			FileManager.replaceTextInFile(resourcesPath + "\\application.properties", "[url]", url);
			FileManager.replaceTextInFile(resourcesPath + "\\application.properties", "[username]",
					jdbcManager.getUsername());

			FileManager.replaceTextInFile(resourcesPath + "\\application.properties", "[password]",
					jdbcManager.getPassword());

			FileManager.replaceTextInFile(resourcesPath + "\\application.properties", "[dialect]",
					PropertiesReading.getProperty(jdbcManager.getServer() + ".dialect"));

			FileManager.replaceTextInFile(resourcesPath + "\\application.properties", "[projectName]", projectName);

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

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
	
	private String convertTextToCamelCase(String text) {
		
		String[] data = text.split("_");
		if(data.length > 1) {
			String aux = data[0].toLowerCase();
			for (int i = 1; i < data.length; i++) {				
				aux += data[i].substring(0, 1).toLowerCase() + data[i].substring(1, data[i].length());
			}
			text = aux;
		}
		else {
			text = text.substring(0, 1).toLowerCase() + text.substring(1, text.length());
		}
		
		return text;
	}

	public boolean generateModel(String tableName, List<Column> columns) {

		try {

			String pathModel = packagePath + "\\" + packageName.replace(".", "\\") + "\\model\\"
					+ capitalizeText(tableName) + ".java";
			File f = new File(pathModel);

			if (f.exists()) {
				System.out.println("");
				return true;
			} else if (columns == null)
				columns = jdbcManager.getColumnsByTable(databaseName, tableName);

			f.createNewFile();
			Writer w = new OutputStreamWriter(new FileOutputStream(f));

			w.append("package " + packageName + ".model;\n\n");

			w.append("import javax.persistence.*;\n");

			w.append("@Entity\n");
			w.append("@Table(name = \"" + tableName + "\")\n");
			w.append("public class " + capitalizeText(tableName) + "{ \n\n");

			// Add properties
			for (Column column : columns) {
				if (!column.getIsForeigKey()) {
					if (column.getIsPrimaryKey()) {
						w.append("\t@Id\n");
						w.append("\t@GeneratedValue(strategy= GenerationType.IDENTITY)\n");
					}
					w.append("\t@Column(name = \"" + column.getName() + "\")\n");
					w.append("\tprivate " + getDataTypeJava(this.server, column.getDataType()) + " "
							+ column.getName().toLowerCase() + ";\n\n");
				} else {
					String foreignKeyColumn = capitalizeText(column.getName().replace("_id", ""));
					w.append("\t@ManyToOne\n");
					w.append("\tprivate " + foreignKeyColumn + " " + foreignKeyColumn.toLowerCase() + ";\n\n");
				}
			}

			// Add constructor

			w.append("\tpublic " + capitalizeText(tableName) + "(){\n");
			w.append("\t}\n\n");

			// Add method setters and getters

			for (Column column : columns) {

				if (!column.getIsForeigKey()) {
					w.append("\tpublic " + getDataTypeJava(this.server, column.getDataType()) + " get"
							+ capitalizeText(column.getName().toLowerCase()) + "(){\n");
					w.append("\t\treturn " + column.getName().toLowerCase() + ";\n");
					w.append("\t}\n\n");

					w.append("\tpublic void set" + capitalizeText(column.getName().toLowerCase()) + "("
							+ getDataTypeJava(this.server, column.getDataType()) + " " + column.getName().toLowerCase()
							+ "){\n");
					w.append("\t\tthis." + column.getName().toLowerCase() + " = " + column.getName().toLowerCase()
							+ ";\n");
					w.append("\t}\n\n");
				}
				else {
					String foreignKeyColumn = capitalizeText(column.getName().replace("_id", ""));
					w.append("\tpublic " + foreignKeyColumn + " get"
							+ foreignKeyColumn + "(){\n");
					w.append("\t\treturn " + foreignKeyColumn.toLowerCase() + ";\n");
					w.append("\t}\n\n");

					w.append("\tpublic void set" + foreignKeyColumn + "("
							+ foreignKeyColumn + " " + foreignKeyColumn.toLowerCase()
							+ "){\n");
					w.append("\t\tthis." + foreignKeyColumn.toLowerCase() + " = " + foreignKeyColumn.toLowerCase()
							+ ";\n");
					w.append("\t}\n\n");
				}
			}

			w.append("}");
			w.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			return false;
		}

		return true;
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
			return false;
		}

		return true;
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
			return false;
		}

		return true;
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
			return false;
		}
		return true;
	}

	private void addTablesSpringSecurity(String dataBaseName) {

		jdbcManager.connect(dataBaseName);
		ScriptRunner runner = new ScriptRunner(jdbcManager.getConnection(), addOAuth2, addOAuth2);
		try {
			InputStream is = new FileInputStream(PropertiesReading.folder_codegenerator_util + "/"
					+ jdbcManager.getServer() + ".InicioSpringSecurity.sql");
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));

			Reader readerAux = replaceDBName(reader, dataBaseName);
			runner.runScript(readerAux);
		} catch (IOException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private Reader replaceDBName(BufferedReader reader, String dataBaseName) {

		String line;
		Reader stringReader = null;
		try {
			line = reader.readLine();
			StringBuffer text = new StringBuffer();

			while (line != null) {
				String lineAux = line.replace("[DataBaseName]", dataBaseName);
				text.append(lineAux).append("\n");
				line = reader.readLine();
			}

			stringReader = new StringReader(text.toString());
			return stringReader;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return stringReader;
	}

}
