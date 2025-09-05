package com.routine.pusher.infrastructure.arch;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.beans.factory.annotation.Autowired;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMembers;
import static com.tngtech.archunit.library.GeneralCodingRules.*;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;
import static com.tngtech.archunit.library.plantuml.rules.PlantUmlArchCondition.Configuration.consideringOnlyDependenciesInDiagram;
import static com.tngtech.archunit.library.plantuml.rules.PlantUmlArchCondition.adhereToPlantUmlDiagram;

@AnalyzeClasses(packages = "com.routine.pusher", importOptions = ImportOption.DoNotIncludeTests.class)
public class ArchitecturalTest
{
    private static final String PKG_MATCHER_RESOURCES          = "..controller";
    private static final String PKG_MATCHER_CLIENT             = "..clients";
    private static final String PKG_MATCHER_SERVICES           = "..service.interface";
    private static final String PKG_MATCHER_SERVICES_IMPL      = "..service";
    private static final String PKG_MATCHER_MAPPERS            = "..mappers";
    private static final String PKG_MATCHER_MODEL              = "..model";
    private static final String PKG_MATCHER_DTO                = "..dto";
    private static final String PKG_MATCHER_ENTITIES           = "..entity";
    private static final String PKG_MATCHER_CONFIG             = "..config";
    private static final String PKG_MATCHER_JOB                = "..job";

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
    public static final ArchRule regraNomenclatura10 = classes( ).that( ).haveSimpleNameEndingWith( "Resource" ).should( ).resideInAPackage( PKG_MATCHER_RESOURCES );

    @ArchTest
    public static final ArchRule regraNomenclatura20 = classes( ).that( ).haveSimpleNameEndingWith( "Service" ).should( ).resideInAPackage( PKG_MATCHER_SERVICES );

    @ArchTest
    public static final ArchRule regraNomenclatura21 = classes( ).that( ).haveSimpleNameEndingWith( "ServiceImpl" ).should( ).resideInAPackage( PKG_MATCHER_SERVICES_IMPL );

    @ArchTest
    public static final ArchRule regraNomenclatura30 = classes( ).that( ).haveSimpleNameEndingWith( "Mapper" ).should( ).resideInAPackage( PKG_MATCHER_MAPPERS );

    @ArchTest
    public static final ArchRule regraNomenclatura50 = classes( ).that( ).haveSimpleNameEndingWith( "Input" ).or( ).haveSimpleNameEndingWith( "Output" ).should( ).resideInAPackage( PKG_MATCHER_MODEL );

    @ArchTest
    public static final ArchRule regraNomenclatura60 = classes( ).that( ).haveSimpleNameEndingWith( "Configuration" ).should( ).resideInAPackage( PKG_MATCHER_CONFIG );

    @ArchTest
    public static final ArchRule interfacePacoteService = classes( ).that( ).resideInAnyPackage( PKG_MATCHER_SERVICES ).should( ).beInterfaces( );

    /**
     * Regras estruturais
     */
    @ArchTest
    public static final ArchRule semCiclos = slices( ).matching( "br.com.sinqia.(*).." ).should( ).beFreeOfCycles( );

    @ArchTest
    public static final ArchRule estrutura = classes( )
            .should( adhereToPlantUmlDiagram( ArchitecturalTest.class.getResource( "/estrutura.plantuml" ), consideringOnlyDependenciesInDiagram( ) ) );
}