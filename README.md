# JCite clone to create Maven artifact

[JCite](http://arrenbrecht.ch/jcite/) also has another clone for providing Maven build capability, [gustafssonallan-mavenbuild](https://code.google.com/r/gustafssonallan-mavenbuild/).

This clone has an artifact published in the Central Repository:

	<dependency>
	 <groupId>org.mcraig</groupId>
	 <artifactId>jcite</artifactId>
	 <version>1.13.0</version>
	</dependency>

*Roll Your Own*

You can build an artifact and install it into your *local* repo with `mvn javadoc:jar install`. The latest in-progress version would call for this dependency.

	<dependency>
	 <groupId>org.mcraig</groupId>
	 <artifactId>jcite</artifactId>
	 <version>1.13.1-SNAPSHOT</version>
	</dependency>

*Limitations*

The Maven POM only helps you build artifacts for JCite and related sources and Javadoc. It does not run tests or build docs.

*Examples*

The idea is that you use the JCite artifact with the Maven Antrun Plugin to preprocess files, as in the following example that runs in the pre-site phase to process some XML files with JCite before building docs:

	<plugin>
	 <groupId>org.apache.maven.plugins</groupId>
	 <artifactId>maven-antrun-plugin</artifactId>
	 <version>1.7</version>
	 <configuration>
	  <target>
	   <mkdir dir="${generatedSourceDirectory}" />
	   <taskdef
	    name="jcite"
	    classname="ch.arrenbrecht.jcite.JCiteTask"
	    classpathref="maven.plugin.classpath" />
	   <jcite
	    srcdir="${sourceDirectory}"
	    destdir="${generatedSourceDirectory}">
	    <sourcepath>
	      <pathelement location="src/main/java" />
	    </sourcepath>
	    <include name="**/*.xml" />
	   </jcite>
	  </target>
	 </configuration>
	 <executions>
	  <execution>
	   <id>run-jcite</id>
	   <phase>pre-site</phase>
	   <goals>
	    <goal>run</goal>
	   </goals>
	  </execution>
	 </executions>
	 <dependencies>
	  <dependency>
	   <groupId>org.mcraig</groupId>
	   <artifactId>jcite</artifactId>
	   <version>${jcite.version}</version>
	  </dependency>
	 </dependencies>
	</plugin>

After processing the sources, use the generated sources in your project.

You can also use JCite in Javadoc. See [Using JCite With JavaDoc](http://www.arrenbrecht.ch/jcite/javadoc.htm). The following example shows how you might configure the Maven Javadoc plugin to have JCite process the sources, as long as all sources are in the conventional place.
	
	<plugin>
	 <groupId>org.apache.maven.plugins</groupId>
	 <artifactId>maven-javadoc-plugin</artifactId>
	 <version>2.9</version>
	 <configuration>
	  <taglet>ch.arrenbrecht.jcite.JCiteTaglet</taglet>
	  <tagletArtifact>
	   <groupId>org.mcraig</groupId>
	   <artifactId>jcite</artifactId>
	   <version>1.13.0</version>
	  </tagletArtifact>
	  <sourcepath>${project.build.sourceDirectory}</sourcepath>
	  <additionalJOption>-J-Djcitesourcepath=${project.build.sourceDirectory}</additionalJOption>
	 </configuration>
	</plugin>

*Licenses*

JCite is licensed under a BSD-style license. See LICENSE.html.

The additional code specific to this clone is licensed under [MPL 2.0](http://mozilla.org/MPL/2.0/).
