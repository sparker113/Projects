import React, { useState, useEffect } from 'react';
import { FileText, Download, Loader2 } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { listWellTests } from '../services/wellTestService';
import { format } from 'date-fns';
import { exportAnalysisResults } from '../utils/exportUtils';
import type { WellTest } from '../services/wellTestService';

function Analysis() {
  const navigate = useNavigate();
  const [analysisHistory, setAnalysisHistory] = useState<WellTest[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    async function fetchAnalysisHistory() {
      try {
        const data = await listWellTests();
        setAnalysisHistory(data);
        setError(null);
      } catch (err) {
        setError('Failed to load analysis history. Please try again later.');
        console.error('Error fetching analysis history:', err);
      } finally {
        setLoading(false);
      }
    }

    fetchAnalysisHistory();
  }, []);

  const handleExport = (analysis: WellTest) => {
    exportAnalysisResults(
      analysis.testData,
      analysis.fluidProperties,
      analysis.wellProperties,
      analysis.analysisResults
    );
  };

  const handleViewDetails = (analysis: WellTest) => {
    navigate(`/analysis/${analysis.id}`);
  };

  if (loading) {
    return (
      <div className="container mx-auto px-4 py-6">
        <div className="flex items-center justify-center min-h-[400px]">
          <Loader2 className="h-8 w-8 animate-spin text-primary-600" />
          <span className="ml-2 text-gray-600">Loading analysis history...</span>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="container mx-auto px-4 py-6">
        <div className="bg-red-50 border border-red-200 rounded-lg p-4 text-red-700">
          <p>{error}</p>
        </div>
      </div>
    );
  }

  if (analysisHistory.length === 0) {
    return (
      <div className="container mx-auto px-4 py-6">
        <div className="bg-white rounded-lg shadow-md p-6 text-center">
          <FileText className="h-12 w-12 text-gray-400 mx-auto mb-4" />
          <h3 className="text-lg font-medium text-gray-900 mb-2">No Analysis History</h3>
          <p className="text-gray-600">
            Start by analyzing a well test to see your analysis history here.
          </p>
        </div>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-6">
      <div className="bg-white rounded-lg shadow-md p-6">
        <h2 className="text-2xl font-bold text-gray-800 mb-6">Analysis History</h2>
        
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Date</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Well Name</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Test Type</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Flow Regime</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {analysisHistory.map((analysis) => (
                <tr key={analysis.id}>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {format(new Date(analysis.testDate), 'MMM d, yyyy')}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                    {analysis.wellName}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {analysis.testType}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {analysis.analysisResults.flowRegime?.replace('_', ' ').charAt(0).toUpperCase() + 
                     analysis.analysisResults.flowRegime?.replace('_', ' ').slice(1) || 'Unknown'}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${
                      analysis.status === 'completed'
                        ? 'bg-green-100 text-green-800'
                        : 'bg-yellow-100 text-yellow-800'
                    }`}>
                      {analysis.status.charAt(0).toUpperCase() + analysis.status.slice(1)}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    <div className="flex space-x-3">
                      <button
                        onClick={() => handleViewDetails(analysis)}
                        className="text-blue-600 hover:text-blue-800 transition-colors flex items-center"
                      >
                        <FileText size={18} />
                      </button>
                      <button
                        onClick={() => handleExport(analysis)}
                        className="text-blue-600 hover:text-blue-800 transition-colors flex items-center"
                      >
                        <Download size={18} />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

export default Analysis;