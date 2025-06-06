import { supabase } from '../lib/supabase';
import { TestData, FluidProperties, WellProperties, AnalysisResults, AnalysisType } from '../types/wellTest';

export interface WellTest {
  id: string;
  wellName: string;
  testType: AnalysisType;
  testDate: string;
  status: string;
  testData: TestData;
  fluidProperties: FluidProperties;
  wellProperties: WellProperties;
  analysisResults: AnalysisResults;
}

export async function saveWellTest(
  wellName: string,
  testType: AnalysisType,
  testData: TestData,
  fluidProperties: FluidProperties,
  wellProperties: WellProperties,
  analysisResults: AnalysisResults
): Promise<string> {
  // Insert well test metadata
  const { data: wellTest, error: wellTestError } = await supabase
    .from('well_tests')
    .insert({
      well_name: wellName,
      test_type: testType,
      status: 'completed'
    })
    .select()
    .single();

  if (wellTestError) throw wellTestError;

  // Insert test data with advanced fields
  const { error: testDataError } = await supabase
    .from('test_data')
    .insert({
      well_test_id: wellTest.id,
      pressure: testData.pressure,
      time: testData.time,
      flow_rate: testData.flowRate,
      pressure_unit: testData.pressureUnit,
      time_unit: testData.timeUnit,
      flow_rate_unit: testData.flowRateUnit,
      shut_in_time: testData.shutInTime,
      gauge_depth: testData.gaugeDepth,
      bhp_correction_method: testData.bhpCorrectionMethod,
      pre_test_rates: testData.preTestRates,
      cumulative_injection: testData.cumulativeInjection
    });

  if (testDataError) throw testDataError;

  // Insert fluid properties with advanced fields
  const { error: fluidPropsError } = await supabase
    .from('fluid_properties')
    .insert({
      well_test_id: wellTest.id,
      viscosity: fluidProperties.viscosity,
      viscosity_unit: fluidProperties.viscosityUnit,
      compressibility: fluidProperties.compressibility,
      formation_volume_factor: fluidProperties.formationVolumeFactor,
      density: fluidProperties.density,
      density_unit: fluidProperties.densityUnit,
      total_compressibility: fluidProperties.totalCompressibility,
      compressibility_unit: fluidProperties.compressibilityUnit
    });

  if (fluidPropsError) throw fluidPropsError;

  // Insert well properties with advanced fields
  const { error: wellPropsError } = await supabase
    .from('well_properties')
    .insert({
      well_test_id: wellTest.id,
      wellbore_radius: wellProperties.wellboreRadius,
      thickness: wellProperties.thickness,
      porosity: wellProperties.porosity,
      length_unit: wellProperties.lengthUnit,
      wellbore_storage: wellProperties.wellboreStorage,
      storage_unit: wellProperties.storageUnit,
      initial_pressure: wellProperties.initialPressure,
      orientation: wellProperties.orientation,
      formation_compressibility: wellProperties.formationCompressibility,
      drainage_radius: wellProperties.drainageRadius,
      boundary_type: wellProperties.boundaryType
    });

  if (wellPropsError) throw wellPropsError;

  // Insert analysis results
  const { error: resultsError } = await supabase
    .from('analysis_results')
    .insert({
      well_test_id: wellTest.id,
      flow_regime: analysisResults.flowRegime,
      transient_results: analysisResults.transient,
      boundary_results: analysisResults.boundary,
      dfit_results: analysisResults.dfit,
      quality_indicators: analysisResults.qualityIndicators
    });

  if (resultsError) throw resultsError;

  return wellTest.id;
}

export async function loadWellTest(id: string): Promise<WellTest> {
  // Fetch well test metadata
  const { data: wellTest, error: wellTestError } = await supabase
    .from('well_tests')
    .select()
    .eq('id', id)
    .single();

  if (wellTestError) throw wellTestError;

  // Fetch test data
  const { data: testData, error: testDataError } = await supabase
    .from('test_data')
    .select()
    .eq('well_test_id', id)
    .single();

  if (testDataError) throw testDataError;

  // Fetch fluid properties
  const { data: fluidProps, error: fluidPropsError } = await supabase
    .from('fluid_properties')
    .select()
    .eq('well_test_id', id)
    .single();

  if (fluidPropsError) throw fluidPropsError;

  // Fetch well properties
  const { data: wellProps, error: wellPropsError } = await supabase
    .from('well_properties')
    .select()
    .eq('well_test_id', id)
    .single();

  if (wellPropsError) throw wellPropsError;

  // Fetch analysis results
  const { data: results, error: resultsError } = await supabase
    .from('analysis_results')
    .select()
    .eq('well_test_id', id)
    .single();

  if (resultsError) throw resultsError;

  return {
    id: wellTest.id,
    wellName: wellTest.well_name,
    testType: wellTest.test_type as AnalysisType,
    testDate: wellTest.test_date,
    status: wellTest.status,
    testData: {
      pressure: testData.pressure,
      time: testData.time,
      flowRate: testData.flow_rate,
      pressureUnit: testData.pressure_unit,
      timeUnit: testData.time_unit,
      flowRateUnit: testData.flow_rate_unit,
      shutInTime: testData.shut_in_time,
      gaugeDepth: testData.gauge_depth,
      bhpCorrectionMethod: testData.bhp_correction_method,
      preTestRates: testData.pre_test_rates,
      cumulativeInjection: testData.cumulative_injection
    },
    fluidProperties: {
      viscosity: fluidProps.viscosity,
      viscosityUnit: fluidProps.viscosity_unit,
      compressibility: fluidProps.compressibility,
      formationVolumeFactor: fluidProps.formation_volume_factor,
      density: fluidProps.density,
      densityUnit: fluidProps.density_unit,
      totalCompressibility: fluidProps.total_compressibility,
      compressibilityUnit: fluidProps.compressibility_unit
    },
    wellProperties: {
      wellboreRadius: wellProps.wellbore_radius,
      thickness: wellProps.thickness,
      porosity: wellProps.porosity,
      lengthUnit: wellProps.length_unit,
      wellboreStorage: wellProps.wellbore_storage,
      storageUnit: wellProps.storage_unit,
      initialPressure: wellProps.initial_pressure,
      orientation: wellProps.orientation,
      formationCompressibility: wellProps.formation_compressibility,
      drainageRadius: wellProps.drainage_radius,
      boundaryType: wellProps.boundary_type
    },
    analysisResults: {
      flowRegime: results.flow_regime,
      transient: results.transient_results,
      boundary: results.boundary_results,
      dfit: results.dfit_results,
      qualityIndicators: results.quality_indicators
    }
  };
}

export async function listWellTests(): Promise<WellTest[]> {
  const { data, error } = await supabase
    .from('well_tests')
    .select(`
      id,
      well_name,
      test_type,
      test_date,
      status,
      test_data (
        pressure,
        time,
        flow_rate,
        pressure_unit,
        time_unit,
        flow_rate_unit,
        shut_in_time,
        gauge_depth,
        bhp_correction_method,
        pre_test_rates,
        cumulative_injection
      ),
      fluid_properties (
        viscosity,
        viscosity_unit,
        compressibility,
        formation_volume_factor,
        density,
        density_unit,
        total_compressibility,
        compressibility_unit
      ),
      well_properties (
        wellbore_radius,
        thickness,
        porosity,
        length_unit,
        wellbore_storage,
        storage_unit,
        initial_pressure,
        orientation,
        formation_compressibility,
        drainage_radius,
        boundary_type
      ),
      analysis_results (
        flow_regime,
        transient_results,
        boundary_results,
        dfit_results,
        quality_indicators
      )
    `)
    .order('test_date', { ascending: false });

  if (error) throw error;

  return data.map(test => ({
    id: test.id,
    wellName: test.well_name,
    testType: test.test_type as AnalysisType,
    testDate: test.test_date,
    status: test.status,
    testData: {
      pressure: test.test_data.pressure,
      time: test.test_data.time,
      flowRate: test.test_data.flow_rate,
      pressureUnit: test.test_data.pressure_unit,
      timeUnit: test.test_data.time_unit,
      flowRateUnit: test.test_data.flow_rate_unit,
      shutInTime: test.test_data.shut_in_time,
      gaugeDepth: test.test_data.gauge_depth,
      bhpCorrectionMethod: test.test_data.bhp_correction_method,
      preTestRates: test.test_data.pre_test_rates,
      cumulativeInjection: test.test_data.cumulative_injection
    },
    fluidProperties: {
      viscosity: test.fluid_properties.viscosity,
      viscosityUnit: test.fluid_properties.viscosity_unit,
      compressibility: test.fluid_properties.compressibility,
      formationVolumeFactor: test.fluid_properties.formation_volume_factor,
      density: test.fluid_properties.density,
      densityUnit: test.fluid_properties.density_unit,
      totalCompressibility: test.fluid_properties.total_compressibility,
      compressibilityUnit: test.fluid_properties.compressibility_unit
    },
    wellProperties: {
      wellboreRadius: test.well_properties.wellbore_radius,
      thickness: test.well_properties.thickness,
      porosity: test.well_properties.porosity,
      lengthUnit: test.well_properties.length_unit,
      wellboreStorage: test.well_properties.wellbore_storage,
      storageUnit: test.well_properties.storage_unit,
      initialPressure: test.well_properties.initial_pressure,
      orientation: test.well_properties.orientation,
      formationCompressibility: test.well_properties.formation_compressibility,
      drainageRadius: test.well_properties.drainage_radius,
      boundaryType: test.well_properties.boundary_type
    },
    analysisResults: {
      flowRegime: test.analysis_results.flow_regime,
      transient: test.analysis_results.transient_results,
      boundary: test.analysis_results.boundary_results,
      dfit: test.analysis_results.dfit_results,
      qualityIndicators: test.analysis_results.quality_indicators
    }
  }));
}