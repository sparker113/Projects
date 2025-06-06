import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft, Download, ChevronDown, ChevronUp } from 'lucide-react';
import { WellTest } from '../services/wellTestService';
import { exportAnalysisResults } from '../utils/exportUtils';
import DiagnosticPlots from './DiagnosticPlots';
import AnalysisResults from './AnalysisResults';
import DFITAnalysisResults from './DFITAnalysisResults';
import ClosureAnalysis from './ClosureAnalysis';
import AfterClosureAnalysis from './AfterClosureAnalysis';
import { format } from 'date-fns';

interface WellTestDetailsProps {
  wellTest: WellTest;
}

const WellTestDetails: React.FC<WellTestDetailsProps> = ({ wellTest }) => {
  const navigate = useNavigate();
  const [showAdvanced, setShowAdvanced] = useState(false);

  const handleExport = () => {
    exportAnalysisResults(
      wellTest.testData,
      wellTest.fluidProperties,
      wellTest.wellProperties,
      wellTest.analysisResults
    );
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <button
          onClick={() => navigate(-1)}
          className="flex items-center text-gray-600 hover:text-gray-900"
        >
          <ArrowLeft size={20} className="mr-1" />
          Back
        </button>
        <button
          onClick={handleExport}
          className="flex items-center px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors"
        >
          <Download size={18} className="mr-2" />
          Export Results
        </button>
      </div>

      <div className="bg-white rounded-lg shadow-md p-6">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <div>
            <h3 className="text-sm font-medium text-gray-500">Well Name</h3>
            <p className="mt-1 text-lg font-semibold text-gray-900">{wellTest.wellName}</p>
          </div>
          <div>
            <h3 className="text-sm font-medium text-gray-500">Test Type</h3>
            <p className="mt-1 text-lg font-semibold text-gray-900">{wellTest.testType}</p>
          </div>
          <div>
            <h3 className="text-sm font-medium text-gray-500">Test Date</h3>
            <p className="mt-1 text-lg font-semibold text-gray-900">
              {format(new Date(wellTest.testDate), 'MMM d, yyyy HH:mm')}
            </p>
          </div>
        </div>
      </div>

      <div className="bg-white rounded-lg shadow-md p-6">
        <h2 className="text-xl font-semibold mb-4">Test Data</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <div>
            <h3 className="text-sm font-medium text-gray-500">Pressure Unit</h3>
            <p className="mt-1 text-gray-900">{wellTest.testData.pressureUnit}</p>
          </div>
          <div>
            <h3 className="text-sm font-medium text-gray-500">Time Unit</h3>
            <p className="mt-1 text-gray-900">{wellTest.testData.timeUnit}</p>
          </div>
          <div>
            <h3 className="text-sm font-medium text-gray-500">Flow Rate Unit</h3>
            <p className="mt-1 text-gray-900">{wellTest.testData.flowRateUnit}</p>
          </div>
        </div>

        {wellTest.testData.shutInTime && (
          <div className="mt-4 pt-4 border-t border-gray-200">
            <button
              onClick={() => setShowAdvanced(!showAdvanced)}
              className="flex items-center text-sm text-gray-600 hover:text-gray-900"
            >
              {showAdvanced ? <ChevronUp size={16} /> : <ChevronDown size={16} />}
              <span className="ml-1">Advanced Test Data</span>
            </button>

            {showAdvanced && (
              <div className="mt-4 grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                <div>
                  <h3 className="text-sm font-medium text-gray-500">Shut-in Time</h3>
                  <p className="mt-1 text-gray-900">
                    {wellTest.testData.shutInTime ? 
                      format(new Date(wellTest.testData.shutInTime), 'MMM d, yyyy HH:mm') : 
                      'Not specified'}
                  </p>
                </div>
                <div>
                  <h3 className="text-sm font-medium text-gray-500">Gauge Depth</h3>
                  <p className="mt-1 text-gray-900">
                    {wellTest.testData.gaugeDepth ? 
                      `${wellTest.testData.gaugeDepth} ft` : 
                      'Not specified'}
                  </p>
                </div>
                <div>
                  <h3 className="text-sm font-medium text-gray-500">BHP Correction Method</h3>
                  <p className="mt-1 text-gray-900 capitalize">
                    {wellTest.testData.bhpCorrectionMethod || 'Not specified'}
                  </p>
                </div>
                {wellTest.testData.preTestRates && (
                  <div className="col-span-full">
                    <h3 className="text-sm font-medium text-gray-500">Pre-Test Flow Rates</h3>
                    <div className="mt-2 overflow-x-auto">
                      <table className="min-w-full divide-y divide-gray-200">
                        <thead>
                          <tr>
                            <th className="px-4 py-2 text-left text-xs font-medium text-gray-500">Time</th>
                            <th className="px-4 py-2 text-left text-xs font-medium text-gray-500">Rate</th>
                          </tr>
                        </thead>
                        <tbody className="divide-y divide-gray-200">
                          {wellTest.testData.preTestRates.map((rate, index) => (
                            <tr key={index}>
                              <td className="px-4 py-2 text-sm text-gray-900">{rate.time}</td>
                              <td className="px-4 py-2 text-sm text-gray-900">{rate.rate}</td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  </div>
                )}
                {wellTest.testType === 'DFIT' && (
                  <div>
                    <h3 className="text-sm font-medium text-gray-500">Cumulative Injection</h3>
                    <p className="mt-1 text-gray-900">
                      {wellTest.testData.cumulativeInjection ? 
                        `${wellTest.testData.cumulativeInjection} bbl` : 
                        'Not specified'}
                    </p>
                  </div>
                )}
              </div>
            )}
          </div>
        )}
      </div>

      <div className="bg-white rounded-lg shadow-md p-6">
        <h2 className="text-xl font-semibold mb-4">Fluid Properties</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <div>
            <h3 className="text-sm font-medium text-gray-500">Viscosity</h3>
            <p className="mt-1 text-gray-900">
              {wellTest.fluidProperties.viscosity} {wellTest.fluidProperties.viscosityUnit}
            </p>
          </div>
          <div>
            <h3 className="text-sm font-medium text-gray-500">Compressibility</h3>
            <p className="mt-1 text-gray-900">{wellTest.fluidProperties.compressibility}</p>
          </div>
          <div>
            <h3 className="text-sm font-medium text-gray-500">Formation Volume Factor</h3>
            <p className="mt-1 text-gray-900">{wellTest.fluidProperties.formationVolumeFactor}</p>
          </div>

          {(wellTest.fluidProperties.density || wellTest.fluidProperties.totalCompressibility) && (
            <div className="col-span-full mt-4 pt-4 border-t border-gray-200">
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {wellTest.fluidProperties.density && (
                  <div>
                    <h3 className="text-sm font-medium text-gray-500">Fluid Density</h3>
                    <p className="mt-1 text-gray-900">
                      {wellTest.fluidProperties.density} {wellTest.fluidProperties.densityUnit}
                    </p>
                  </div>
                )}
                {wellTest.fluidProperties.totalCompressibility && (
                  <div>
                    <h3 className="text-sm font-medium text-gray-500">Total Compressibility</h3>
                    <p className="mt-1 text-gray-900">
                      {wellTest.fluidProperties.totalCompressibility} {wellTest.fluidProperties.compressibilityUnit}
                    </p>
                  </div>
                )}
              </div>
            </div>
          )}
        </div>
      </div>

      <div className="bg-white rounded-lg shadow-md p-6">
        <h2 className="text-xl font-semibold mb-4">Well Properties</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <div>
            <h3 className="text-sm font-medium text-gray-500">Wellbore Radius</h3>
            <p className="mt-1 text-gray-900">
              {wellTest.wellProperties.wellboreRadius} {wellTest.wellProperties.lengthUnit}
            </p>
          </div>
          <div>
            <h3 className="text-sm font-medium text-gray-500">Thickness</h3>
            <p className="mt-1 text-gray-900">
              {wellTest.wellProperties.thickness} {wellTest.wellProperties.lengthUnit}
            </p>
          </div>
          <div>
            <h3 className="text-sm font-medium text-gray-500">Porosity</h3>
            <p className="mt-1 text-gray-900">{wellTest.wellProperties.porosity}</p>
          </div>

          {(wellTest.wellProperties.wellboreStorage || 
            wellTest.wellProperties.initialPressure || 
            wellTest.wellProperties.orientation || 
            wellTest.wellProperties.formationCompressibility || 
            wellTest.wellProperties.drainageRadius || 
            wellTest.wellProperties.boundaryType) && (
            <div className="col-span-full mt-4 pt-4 border-t border-gray-200">
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {wellTest.wellProperties.wellboreStorage && (
                  <div>
                    <h3 className="text-sm font-medium text-gray-500">Wellbore Storage</h3>
                    <p className="mt-1 text-gray-900">
                      {wellTest.wellProperties.wellboreStorage} {wellTest.wellProperties.storageUnit}
                    </p>
                  </div>
                )}
                {wellTest.wellProperties.initialPressure && (
                  <div>
                    <h3 className="text-sm font-medium text-gray-500">Initial Pressure</h3>
                    <p className="mt-1 text-gray-900">
                      {wellTest.wellProperties.initialPressure} psi
                    </p>
                  </div>
                )}
                {wellTest.wellProperties.orientation && (
                  <div>
                    <h3 className="text-sm font-medium text-gray-500">Well Orientation</h3>
                    <p className="mt-1 text-gray-900 capitalize">
                      {wellTest.wellProperties.orientation}
                    </p>
                  </div>
                )}
                {wellTest.wellProperties.formationCompressibility && (
                  <div>
                    <h3 className="text-sm font-medium text-gray-500">Formation Compressibility</h3>
                    <p className="mt-1 text-gray-900">
                      {wellTest.wellProperties.formationCompressibility}
                    </p>
                  </div>
                )}
                {wellTest.wellProperties.drainageRadius && (
                  <div>
                    <h3 className="text-sm font-medium text-gray-500">Drainage Radius</h3>
                    <p className="mt-1 text-gray-900">
                      {wellTest.wellProperties.drainageRadius} {wellTest.wellProperties.lengthUnit}
                    </p>
                  </div>
                )}
                {wellTest.wellProperties.boundaryType && (
                  <div>
                    <h3 className="text-sm font-medium text-gray-500">Boundary Type</h3>
                    <p className="mt-1 text-gray-900 capitalize">
                      {wellTest.wellProperties.boundaryType.replace('-', ' ')}
                    </p>
                  </div>
                )}
              </div>
            </div>
          )}
        </div>
      </div>

      <div className="bg-white rounded-lg shadow-md p-6">
        <DiagnosticPlots
          testData={wellTest.testData}
          identifiedFlowRegime={wellTest.analysisResults.flowRegime || null}
          analysisType={wellTest.testType}
        />
      </div>

      {wellTest.testType === 'PBU' ? (
        <AnalysisResults
          results={wellTest.analysisResults}
          flowRegime={wellTest.analysisResults.flowRegime || null}
          onManualRegimeChange={() => {}}
          defaultFlags={{
            viscosity: false,
            compressibility: false,
            formationVolumeFactor: false,
            wellboreRadius: false,
            thickness: false,
            porosity: false,
            fluidDensity: false,
            totalCompressibility: false,
            wellboreStorage: false,
            initialPressure: false,
            formationCompressibility: false,
            drainageRadius: false
          }}
        />
      ) : (
        <>
          <ClosureAnalysis testData={wellTest.testData} />
          <AfterClosureAnalysis 
            testData={wellTest.testData}
            fluidViscosity={wellTest.fluidProperties.viscosity}
            totalCompressibility={wellTest.fluidProperties.totalCompressibility || 1e-5}
            porosity={wellTest.wellProperties.porosity}
            thickness={wellTest.wellProperties.thickness}
          />
          <DFITAnalysisResults
            results={wellTest.analysisResults.dfit}
            defaultFlags={{
              viscosity: false,
              compressibility: false,
              formationVolumeFactor: false,
              wellboreRadius: false,
              thickness: false,
              porosity: false,
              fluidDensity: false,
              totalCompressibility: false,
              wellboreStorage: false,
              initialPressure: false,
              formationCompressibility: false,
              drainageRadius: false
            }}
          />
        </>
      )}
    </div>
  );
};

export default WellTestDetails;