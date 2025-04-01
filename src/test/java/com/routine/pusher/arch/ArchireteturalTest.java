//package com.routine.pusher.arch;
//
//import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
//import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMembers;
//import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS;
//import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS;
//import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING;
//import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;
//import static com.tngtech.archunit.library.plantuml.rules.PlantUmlArchCondition.adhereToPlantUmlDiagram;
//import static com.tngtech.archunit.library.plantuml.rules.PlantUmlArchCondition.Configuration.consideringOnlyDependenciesInDiagram;
//
//import org.springframework.beans.factory.annotation.Autowired;
//
//import com.tngtech.archunit.core.importer.ImportOption;
//import com.tngtech.archunit.junit.AnalyzeClasses;
//import com.tngtech.archunit.junit.ArchTest;
//import com.tngtech.archunit.lang.ArchRule;
//
//@AnalyzeClasses(packages = "br.com.sinqia.fundos.sqfaas.frontend.bff", importOptions = ImportOption.DoNotIncludeTests.class)
//public class ArchitecturalTest
//{
//    private static final String PKG_MATCHER_RESOURCES          = "..api";
//    private static final String PKG_MATCHER_SERVICES           = "..services";
//    private static final String PKG_MATCHER_SERVICES_IMPL      = "..services.impl";
//    private static final String PKG_MATCHER_MAPPERS            = "..mappers";
//    private static final String PKG_MATCHER_MODEL              = "..model";
//    private static final String PKG_MATCHER_CONFIG             = "..config";
//    private static final String PKG_MATCHER_INTEGRATION        = "..integrations";
//    private static final String PKG_MATCHER_INTEGRATION_CLIENT = "..integrations.*.clients";
//    private static final String PKG_MATCHER_INTEGRATION_DTO    = "..integrations.*.dto";
//    private static final String PKG_MATCHER_INTEGRATION_IMPL   = "..integrations.*.impl";
//
//    @ArchTest
//    public static final ArchRule noAccessStreams = NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS;
//
//    @ArchTest
//    public static final ArchRule noGenericExcpetions = NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS;
//
//    @ArchTest
//    public static final ArchRule noJavaLogging = NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING;
//
//    /**
//     * Ao invés de usar @Autowired, as variáveis membro devem usar @Inject.
//     */
//    @ArchTest
//    public static final ArchRule noClassUsesAutowired = noMembers( ).should( ).beAnnotatedWith( Autowired.class ).because( "should use @Inject" );
//
//    /**
//     * Regras de nomenclatura de classes/pacotes
//     */
//    @ArchTest
//    public static final ArchRule regraNomenclatura10 = classes( ).that( ).haveSimpleNameEndingWith( "Resource" ).should( ).resideInAPackage( PKG_MATCHER_RESOURCES );
//
//    @ArchTest
//    public static final ArchRule regraNomenclatura20 = classes( ).that( ).haveSimpleNameEndingWith( "Service" ).should( ).resideInAPackage( PKG_MATCHER_SERVICES );
//
//    @ArchTest
//    public static final ArchRule regraNomenclatura21 = classes( ).that( ).haveSimpleNameEndingWith( "ServiceImpl" ).should( ).resideInAPackage( PKG_MATCHER_SERVICES_IMPL );
//
//    @ArchTest
//    public static final ArchRule regraNomenclatura30 = classes( ).that( ).haveSimpleNameEndingWith( "Mapper" ).should( ).resideInAPackage( PKG_MATCHER_MAPPERS );
//
//    @ArchTest
//    public static final ArchRule regraNomenclatura40 = classes( ).that( ).haveSimpleNameEndingWith( "Integration" ).should( ).resideInAPackage( PKG_MATCHER_INTEGRATION );
//
//    @ArchTest
//    public static final ArchRule regraNomenclatura41 = classes( ).that( ).haveSimpleNameEndingWith( "IntegrationImpl" ).should( ).resideInAPackage( PKG_MATCHER_INTEGRATION_IMPL );
//
//    @ArchTest
//    public static final ArchRule regraNomenclatura42 = classes( ).that( ).haveSimpleNameEndingWith( "Client" ).should( ).resideInAPackage( PKG_MATCHER_INTEGRATION_CLIENT );
//
//    @ArchTest
//    public static final ArchRule regraNomenclatura43 = classes( ).that( ).haveSimpleNameEndingWith( "Dto" ).or( ).haveSimpleNameEndingWith( "RespostaDto" ).should( ).resideInAPackage( PKG_MATCHER_INTEGRATION_DTO );
//
//    @ArchTest
//    public static final ArchRule regraNomenclatura50 = classes( ).that( ).haveSimpleNameEndingWith( "Input" ).or( ).haveSimpleNameEndingWith( "Output" ).should( ).resideInAPackage( PKG_MATCHER_MODEL );
//
//    @ArchTest
//    public static final ArchRule regraNomenclatura60 = classes( ).that( ).haveSimpleNameEndingWith( "Configuration" ).should( ).resideInAPackage( PKG_MATCHER_CONFIG );
//
//    /**
//     * Regras dos pacotes só com interfaces
//     */
//    @ArchTest
//    public static final ArchRule interfacePacoteIntegration = classes( ).that( ).resideInAnyPackage( PKG_MATCHER_INTEGRATION ).should( ).beInterfaces( );
//
//    @ArchTest
//    public static final ArchRule interfacePacoteService = classes( ).that( ).resideInAnyPackage( PKG_MATCHER_SERVICES ).should( ).beInterfaces( );
//
//    /**
//     * Regras estruturais
//     */
//    @ArchTest
//    public static final ArchRule semCiclos = slices( ).matching( "br.com.sinqia.(*).." ).should( ).beFreeOfCycles( );
//
//    @ArchTest
//    public static final ArchRule estrutura = classes( )
//            .should( adhereToPlantUmlDiagram( ArchitecturalTest.class.getResource( "/estrutura.plantuml" ), consideringOnlyDependenciesInDiagram( ) ) );
//}