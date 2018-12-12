# drools-pkg-maven-plugin

build rules and make binary package file(pkg).

version 3.0.0 is for drools 7.11.x or RHDM 7.1.x.

_caution : Currently binary package file is not supported by RedHat. Please use this plugin at your own risk._

## usage

```xml:pom.xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.mk300</groupId>
        <artifactId>drools-pkg-plugin-test</artifactId>
        <version>1.0.0</version>
    </parent>
    <artifactId>drools-pkg-plugin-test-build</artifactId>

    <properties>

        <!-- drools/RHDM version you choice -->
        <brms-version>7.11.0.Final-redhat-00004</brms-version>

        <!-- rule compile options -->
        <drools-pkg.encoding>UTF-8</drools-pkg.encoding>
        <drools-pkg.ruleBaseDir>${project.basedir}/src/main/resources</drools-pkg.ruleBaseDir>
        <drools-pkg.pkgDir>${project.build.directory}</drools-pkg.pkgDir>
        <drools-pkg.pkgFileName>test.pkg</drools-pkg.pkgFileName>
        <drools-pkg.stream>false</drools-pkg.stream>
        <drools-pkg.mvel.strict>false</drools-pkg.mvel.strict>

        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <maven.compiler.target>1.8</maven.compiler.target>
        <maven.compiler.source>1.8</maven.compiler.source>

    </properties>

    <dependencies>
        <!-- Your rules dependent artifacts(Fact,Function,etc) -->
        <dependency>
            <groupId>org.mk300</groupId>
            <artifactId>drools-pkg-plugin-test-fact</artifactId>
            <version>1.0.0</version>
        </dependency>
    </dependencies>


    <build>
        <defaultGoal>package</defaultGoal>
        <plugins>
            <plugin>
                <groupId>org.mk300</groupId>
                <artifactId>drools-pkg-maven-plugin</artifactId>
                <version>3.0.0</version>
                <dependencies>
                    <!-- drools/RHDM artifacts used by rule compiling and generating pkg -->
                    <dependency>
                        <groupId>org.drools</groupId>
                        <artifactId>drools-compiler</artifactId>
                        <version>${brms-version}</version>
                    </dependency>
                    <!-- required for decision-table -->
                    <dependency>
                        <groupId>org.drools</groupId>
                        <artifactId>drools-decisiontables</artifactId>
                        <version>${brms-version}</version>
                    </dependency>
                    <!-- required for score-cards -->
                    <dependency>
                        <groupId>org.drools</groupId>
                        <artifactId>drools-scorecards</artifactId>
                        <version>${brms-version}</version>
                    </dependency>
                    <!-- required for rule-flow -->
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
</project>
```


Use package

```java
    public static void main(String[] args) throws Exception {
        
        KieServices kieServices = KieServices.Factory.get();
        KieBase kbase = new KieHelper().addResource(kieServices.getResources().newFileSystemResource("drools-pkg-test.pkg")).build();
        
        KieSession session = kbase.newKieSession();
        
        session.insert(new MyPojo());
        session.fireAllRules();
        session.dispose();
    }
```
