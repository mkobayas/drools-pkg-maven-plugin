    <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    	<modelVersion>4.0.0</modelVersion>

    	<groupId>org.mk300.example</groupId>
    	<artifactId>drools-pkg-test</artifactId>
    	<version>0.0.1-SNAPSHOT</version>

    	<properties>

    		<!-- BRMS version you choice -->
    		<brms-version>6.0.3-redhat-6</brms-version>

    		<!-- Rule File encoding -->
    		<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>

    	</properties>

    	<dependencies>

    		<!-- Your rules dependent artifacts(Fact,Function,etc) -->
    		<dependency>
    			<groupId>org.mk300.example</groupId>
    			<artifactId>realtime-big-data-common</artifactId>
    			<version>1.0.0</version>
    		</dependency>
    		<dependency>
    			<groupId>org.jboss.logging</groupId>
    			<artifactId>jboss-logging</artifactId>
    			<version>3.1.4.GA-redhat-1</version>
    		</dependency>

    	</dependencies>


    	<build>
    		<defaultGoal>package</defaultGoal>
    		<plugins>
    			<plugin>
    				<groupId>org.mk300</groupId>
    				<artifactId>drools-pkg-maven-plugin</artifactId>
    				<version>1.0.3</version>
    				<dependencies>
    					<!-- BRMS artifacts used by rule compiling and generating pkg -->
    					<dependency>
    						<groupId>org.drools</groupId>
    						<artifactId>drools-compiler</artifactId>
    						<version>${brms-version}</version>
    					</dependency>
    					<!-- BRMS required for decision-table -->
    					<dependency>
    						<groupId>org.drools</groupId>
    						<artifactId>drools-decisiontables</artifactId>
    						<version>${brms-version}</version>
    					</dependency>
    					<!-- BRMS required for score-cards -->
    					<dependency>
    						<groupId>org.drools</groupId>
    						<artifactId>drools-scorecards</artifactId>
    						<version>${brms-version}</version>
    					</dependency>
    					<!-- BRMS required for rule-flow -->
    					<dependency>
    						<groupId>org.jbpm</groupId>
    						<artifactId>jbpm-bpmn2</artifactId>
    						<version>${brms-version}</version>
    					</dependency>
    				</dependencies>
    				<executions>
    					<execution>
    						<id>pkg</id>
    						<phase>package</phase>
    						<goals>
    							<goal>drools-pkg</goal>
    						</goals>
    					</execution>
    				</executions>
    			</plugin>
    			<plugin>
    				<artifactId>maven-jar-plugin</artifactId>
    				<version>2.3.1</version>
    				<executions>
    					<execution>
    						<id>default-jar</id>
    						<phase>none</phase>
    					</execution>
    				</executions>
    			</plugin>
    			<plugin>
    				<artifactId>maven-install-plugin</artifactId>
    				<version>2.3.1</version>
    				<executions>
    					<execution>
    						<id>default-install</id>
    						<phase>none</phase>
    					</execution>
    				</executions>
    			</plugin>
    		</plugins>
    	</build>

    	<repositories>
    		<repository>
    			<id>jboss-ga-repository</id>
    			<url>https://maven.repository.redhat.com/techpreview/all</url>
    			<layout>default</layout>
    			<releases>
    				<enabled>true</enabled>
    				<updatePolicy>never</updatePolicy>
    			</releases>
    			<snapshots>
    				<enabled>false</enabled>
    				<updatePolicy>never</updatePolicy>
    			</snapshots>
    		</repository>
    	</repositories>
    	<pluginRepositories>
    		<pluginRepository>
    			<id>jboss-ga-plugin-repository</id>
    			<url>https://maven.repository.redhat.com/techpreview/all</url>
    			<layout>default</layout>
    			<releases>
    				<enabled>true</enabled>
    				<updatePolicy>never</updatePolicy>
    			</releases>
    			<snapshots>
    				<enabled>false</enabled>
    				<updatePolicy>never</updatePolicy>
    			</snapshots>
    		</pluginRepository>
    	</pluginRepositories>
    </project>