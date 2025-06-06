import React, { useState } from 'react';
import { FileDown, PanelLeft, X, ChevronRight } from 'lucide-react';
import DataInputForm from '../components/DataInputForm';
import DiagnosticPlots from '../components/DiagnosticPlots';
import AnalysisResults from '../components/AnalysisResults';
import DFITAnalysisResults from '../components/DFITAnalysisResults';
import { useNavigate } from 'react-router-dom';
import { FlowRegime, FluidProperties, WellProperties, TestData, AnalysisType, DefaultFlags } from '../types/wellTest';
import { calculateTransientFlow, calculateBoundaryDominatedFlow } from '../utils/analysisCalculations';
import { analyzeDFIT } from '../utils/dfitAnalysis';
import { identifyFlowRegime } from '../utils/flowRegimeIdentification';
import { exportAnalysisResults } from '../utils/exportUtils';
import { saveWellTest } from '../services/wellTestService';

function Dashboard() {
  const navigate = useNavigate();
  const [sidebarOpen, setSidebarOpen] = useState(true);
  const [testData, setTestData] = useState<TestData | null>(null);
  const [fluidProperties, setFluidProperties] = useState<FluidProperties | null>(null);
  const [wellProperties, setWellProperties] = useState<WellProperties | null>(null);
  const [identifiedFlowRegime, setIdentifiedFlowRegime] = useState<FlowRegime | null>(null);
  const [manualFlowRegime, setManualFlowRegime] = useState<FlowRegime | null>(null);
  const [analysisResults, setAnalysisResults] = useState<any>(null);
  const [analysisType, setAnalysisType] = useState<AnalysisType>('PBU');
  const [isCalculating, setIsCalculating] = useState(false);
  const [defaultFlags, setDefaultFlags] = useState<DefaultFlags | null>(null);
  const [isSaving, setIsSaving] = useState(false);
  const [saveError, setSaveError] = useState<string | null>(null);
  const [showSaveDialog, setShowSaveDialog] = useState(false);
  const [wellName, setWellName] = useState('');

  const handleDataSubmit = (
    data: TestData, 
    fluid: FluidProperties, 
    well: WellProperties,
    type: AnalysisType,
    flags: DefaultFlags
  ) => {
    setTestData(data);
    setFluidProperties(fluid);
    setWellProperties(well);
    setAnalysisType(type);
    setDefaultFlags(flags);
    setIsCalculating(true);
    
    if (type === 'PBU') {
      const flowRegime = identifyFlowRegime(data);
      setIdentifiedFlowRegime(flowRegime);
      setManualFlowRegime(flowRegime);
      
      const transientResults = calculateTransientFlow(data, fluid, well, flowRegime);
      const boundaryResults = calculateBoundaryDominatedFlow(data, fluid, well, flowRegime);
      
      setAnalysisResults({
        transient: transientResults,
        boundary: boundaryResults,
        flowRegime: flowRegime,
        analysisType: 'PBU',
        qualityIndicators: {
          regressionQuality: 0.85,
          dataQuality: 0.9,
          interpretationConfidence: 0.87
        }
      });
    } else {
      const dfitResults = analyzeDFIT(data);
      setAnalysisResults({
        dfit: dfitResults,
        analysisType: 'DFIT',
        qualityIndicators: {
          regressionQuality: 0.85,
          dataQuality: 0.9,
          interpretationConfidence: 0.87
        }
      });
    }
    
    setIsCalculating(false);
  };

  const handleExportResults = () => {
    if (!testData || !fluidProperties || !wellProperties || !analysisResults) {
      return;
    }
    
    exportAnalysisResults(
      testData,
      fluidProperties,
      wellProperties,
      analysisResults
    );
  };

  const handleSaveResults = async () => {
    if (!testData || !fluidProperties || !wellProperties || !analysisResults || !wellName.trim()) {
      return;
    }

    setIsSaving(true);
    setSaveError(null);

    try {
      const wellTestId = await saveWellTest(
        wellName.trim(),
        analysisType,
        testData,
        fluidProperties,
        wellProperties,
        analysisResults
      );
      setShowSaveDialog(false);
      navigate(`/analysis/${wellTestId}`);
    } catch (error) {
      console.error('Error saving well test:', error);
      setSaveError('Failed to save well test. Please try again.');
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <div className="flex flex-col md:flex-row flex-1 overflow-hidden bg-gray-50">
      <div 
        className={`bg-white shadow-lg transition-all duration-300 flex flex-col h-full border-r border-gray-200
          ${sidebarOpen ? 'w-full md:w-[420px]' : 'w-12'}`}
      >
        <button 
          onClick={() => setSidebarOpen(!sidebarOpen)}
          className="flex items-center justify-center p-3 bg-primary-900 text-white hover:bg-primary-800 transition-colors"
          aria-label={sidebarOpen ? 'Collapse sidebar' : 'Expand sidebar'}
        >
          <PanelLeft size={20} className={`transform transition-transform ${!sidebarOpen ? 'rotate-180' : ''}`} />
        </button>
        
        {sidebarOpen && (
          <div className="p-6 overflow-y-auto flex-1">
            <h2 className="text-2xl font-bold text-gray-900 mb-6">Input Data</h2>
            <DataInputForm onSubmit={handleDataSubmit} />
          </div>
        )}
      </div>
      
      <div className="flex-1 overflow-y-auto p-6 space-y-6">
        {isCalculating ? (
          <div className="flex flex-col items-center justify-center min-h-[400px] bg-white rounded-xl shadow-lg border border-gray-200 p-8">
            <div className="animate-spin rounded-full h-12 w-12 border-4 border-primary-600 border-t-transparent mb-4"></div>
            <p className="text-xl text-gray-700">Performing analysis...</p>
          </div>
        ) : testData && analysisResults ? (
          <>
            <div className="bg-white rounded-xl shadow-lg border border-gray-200 p-6">
              <div className="flex flex-col md:flex-row md:justify-between md:items-center mb-6 space-y-4 md:space-y-0">
                <h2 className="text-2xl font-bold text-gray-900">Analysis Results</h2>
                <div className="flex gap-3">
                  <button
                    onClick={() => setShowSaveDialog(true)}
                    className="flex items-center gap-2 px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors shadow-sm"
                  >
                    Save Analysis
                  </button>
                  <button
                    onClick={handleExportResults}
                    className="flex items-center gap-2 px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors shadow-sm"
                  >
                    <FileDown size={18} />
                    Export
                  </button>
                </div>
              </div>
              <DiagnosticPlots 
                testData={testData} 
                identifiedFlowRegime={identifiedFlowRegime}
                analysisType={analysisType}
              />
            </div>
            
            {analysisType === 'PBU' ? (
              <AnalysisResults 
                results={analysisResults} 
                flowRegime={manualFlowRegime}
                onManualRegimeChange={setManualFlowRegime}
                defaultFlags={defaultFlags!}
              />
            ) : (
              <DFITAnalysisResults 
                results={analysisResults.dfit}
                defaultFlags={defaultFlags!}
              />
            )}
          </>
        ) : (
          <div className="bg-gradient-to-br from-white to-gray-50 rounded-xl shadow-lg border border-gray-200 p-8">
            <div className="text-center max-w-2xl mx-auto">
              <div className="bg-primary-100 rounded-full p-4 w-16 h-16 mx-auto mb-6 flex items-center justify-center">
                <ChevronRight size={32} className="text-primary-600" />
              </div>
              <h2 className="text-3xl font-extrabold text-gray-900 mb-4">Well Test Analysis</h2>
              <p className="text-lg text-gray-600 mb-8">
                Use the input form to enter pressure, time, and flow rate data along with fluid and well properties.
                The analysis results and diagnostic plots will appear here once data is submitted.
              </p>
              {!sidebarOpen && (
                <button
                  onClick={() => setSidebarOpen(true)}
                  className="inline-flex items-center px-6 py-3 text-lg font-medium text-white bg-primary-600 rounded-lg hover:bg-primary-700 transition-colors shadow-md"
                >
                  Get Started
                  <ChevronRight size={20} className="ml-2" />
                </button>
              )}
            </div>
          </div>
        )}
      </div>

      {/* Save Dialog */}
      {showSaveDialog && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-xl shadow-xl max-w-md w-full p-6">
            <div className="flex justify-between items-center mb-6">
              <h3 className="text-xl font-bold text-gray-900">Save Analysis</h3>
              <button
                onClick={() => setShowSaveDialog(false)}
                className="text-gray-400 hover:text-gray-500 transition-colors"
              >
                <X size={20} />
              </button>
            </div>

            {saveError && (
              <div className="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg text-red-700 text-sm">
                {saveError}
              </div>
            )}

            <div className="mb-6">
              <label htmlFor="wellName" className="block text-sm font-medium text-gray-700 mb-2">
                Well Name
              </label>
              <input
                type="text"
                id="wellName"
                value={wellName}
                onChange={(e) => setWellName(e.target.value)}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg shadow-sm focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
                placeholder="Enter well name"
              />
            </div>

            <div className="flex justify-end gap-3">
              <button
                onClick={() => setShowSaveDialog(false)}
                className="px-4 py-2 text-sm font-medium text-gray-700 hover:text-gray-800 transition-colors"
                disabled={isSaving}
              >
                Cancel
              </button>
              <button
                onClick={handleSaveResults}
                disabled={isSaving || !wellName.trim()}
                className="px-4 py-2 text-sm font-medium text-white bg-primary-600 rounded-lg hover:bg-primary-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500 disabled:opacity-50 transition-colors shadow-sm"
              >
                {isSaving ? 'Saving...' : 'Save'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default Dashboard;