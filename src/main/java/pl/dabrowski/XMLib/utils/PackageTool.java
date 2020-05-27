package pl.dabrowski.XMLib.utils;

import java.io.IOException;
import java.util.Enumeration;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class PackageTool {
		public static void doForEveryObjectInPackage(String packageName, Consumer<JarEntry> body) {
			String packagePath = getFullPathToPackage(packageName);
			if(packagePath.contains("jar!")) { //if JAR
				packagePath = fromFullPathToClassical(packagePath);
				try (JarFile jf = new JarFile(packagePath)) {
					final Enumeration<JarEntry> entries = jf.entries();
					while (entries.hasMoreElements()) {
						final JarEntry entry = entries.nextElement();
						if(isStringJavaLanguageObject(entry.getName())//
							&& entry.getName().contains("$") == false//
							&& entry.getName().startsWith(packageName.replace(".", "/"))) {
							body.accept(entry);
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		private static String getFullPathToPackage(String packageName) {
			return PackageTool.class.getResource("").toString() + preparePackageString(packageName);
		}

		private static String fromFullPathToClassical(String input) {
			return input//
				.substring(0, input.lastIndexOf(".jar!") + 4)//remove path after .jar!
				.substring("jar:file:/".length());//remove initial protocol
		}

		private static boolean isStringJavaLanguageObject(String input) {
			return input.endsWith(".class");
		}

		private static String preparePackageString(String input) { //@formatter:off
			input = input.replace(".", "/");
			if(input.startsWith("/")) input = input.substring(1); //lose first slash /
			if(input.endsWith("/") == false) input = input + "/"; //add last slash if not present
			return input;
		}//@formatter:on
}
