package com.routine.pusher.infrastructure.arch;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.beans.factory.annotation.Autowired;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMembers;
import static com.tngtech.archunit.library.GeneralCodingRules.*;

@AnalyzeClasses(packages = "com.routine.pusher", importOptions = ImportOption.DoNotIncludeTests.class)
public class ArchitecturalTest
{
    private static final String PKG_MATCHER_CONTROLLERS     = "..controller";
    private static final String PKG_MATCHER_CLIENT          = "..client";
    private static final String PKG_MATCHER_SERVICES        = "..service";
    private static final String PKG_MATCHER_USE_CASES       = "..usecase";
    private static final String PKG_MATCHER_DTO             = "..dto";
    private static final String PKG_MATCHER_CONFIG          = "..config";
    private static final String PKG_MATCHER_JOB             = "..job";

    @ArchTest
    public static final ArchRule noAccessStreams = NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS;

    @ArchTest
    public static final ArchRule noGenericExcpetions = NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS;

    @ArchTest
    public static final ArchRule noJavaLogging = NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING;

    /**
     * Ao invés de usar @Autowired, as variáveis membro devem usar @Inject.
     */
    @ArchTest
    public static final ArchRule noClassUsesAutowired = noMembers( ).should( ).beAnnotatedWith( Autowired.class ).because( "should use @Inject" );

    /**
     * Regras de nomenclatura de classes/pacotes
     */
    @ArchTest
    public static final ArchRule regraNomenclatura10 = classes( ).that( ).haveSimpleNameEndingWith( "Controller" ).should( ).resideInAPackage( PKG_MATCHER_CONTROLLERS );

    @ArchTest
    public static final ArchRule regraNomenclatura20 = classes( ).that( ).haveSimpleNameEndingWith( "Service" ).should( ).resideInAPackage( PKG_MATCHER_SERVICES );

    @ArchTest
    public static final ArchRule regraNomenclatura30 = classes( ).that( ).haveSimpleNameEndingWith( "Client" ).should( ).resideInAPackage( PKG_MATCHER_CLIENT );

    @ArchTest
    public static final ArchRule regraNomenclatura40 = classes( ).that( ).haveSimpleNameEndingWith( "InputDTO" ).or( ).haveSimpleNameEndingWith( "OutputDTO" ).should( ).resideInAPackage( PKG_MATCHER_DTO );

    @ArchTest
    public static final ArchRule regraNomenclatura50 = classes( ).that( ).haveSimpleNameEndingWith( "Config" ).should( ).resideInAPackage( PKG_MATCHER_CONFIG );

    @ArchTest
    public static final ArchRule regraNomenclatura60 = classes( ).that( ).haveSimpleNameEndingWith( "Job" ).should( ).resideInAPackage( PKG_MATCHER_JOB );

    @ArchTest
    public static final ArchRule interfacePacoteUseCase = classes( ).that( ).resideInAnyPackage( PKG_MATCHER_USE_CASES ).should( ).beInterfaces( );

    /**
     * Regras estruturais
     */
//    @ArchTest
//    public static final ArchRule semCiclos = slices( ).matching( "br.com.routine.pusher.(*).." ).should( ).beFreeOfCycles( )

//    @ArchTest
//    public static final ArchRule estrutura = classes( )
//            .should( adhereToPlantUmlDiagram( ArchitecturalTest.class.getResource( "/estrutura.plantuml" ), consideringOnlyDependenciesInDiagram( ) ) )
}