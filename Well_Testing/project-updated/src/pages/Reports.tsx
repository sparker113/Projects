import React, { useState, useEffect } from 'react';
import { BarChart, FileText, Download, Loader2 } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { listWellTests } from '../services/wellTestService';
import { format } from 'date-fns';
import { exportAnalysisResults } from '../utils/exportUtils';
import type { WellTest } from '../services/wellTestService';

function Reports() {
  const navigate = useNavigate();
  const [reports, setReports] = useState<WellTest[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    async function fetchReports() {
      try {
        const data = await listWellTests();
        setReports(data);
        setError(null);
      } catch (err) {
        setError('Failed to load reports. Please try again later.');
        console.error('Error fetching reports:', err);
      } finally {
        setLoading(false);
      }
    }

    fetchReports();
  }, []);

  const handleExport = (report: WellTest) => {
    exportAnalysisResults(
      report.testData,
      report.fluidProperties,
      report.wellProperties,
      report.analysisResults
    );
  };

  const handleViewDetails = (report: WellTest) => {
    navigate(`/reports/${report.id}`);
  };

  if (loading) {
    return (
      <div className="container mx-auto px-4 py-6">
        <div className="flex items-center justify-center min-h-[400px]">
          <Loader2 className="h-8 w-8 animate-spin text-primary-600" />
          <span className="ml-2 text-gray-600">Loading reports...</span>
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

  if (reports.length === 0) {
    return (
      <div className="container mx-auto px-4 py-6">
        <div className="bg-white rounded-lg shadow-md p-6 text-center">
          <BarChart className="h-12 w-12 text-gray-400 mx-auto mb-4" />
          <h3 className="text-lg font-medium text-gray-900 mb-2">No Reports Available</h3>
          <p className="text-gray-600">
            Start by analyzing a well test to generate your first report.
          </p>
        </div>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-6">
      <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
        {reports.map((report) => (
          <div key={report.id} className="bg-white rounded-lg shadow-md p-6">
            <div className="flex items-center justify-between mb-4">
              <div className="p-2 bg-blue-100 rounded-lg">
                <BarChart className="h-6 w-6 text-blue-600" />
              </div>
              <span className="text-sm text-gray-500">
                {format(new Date(report.testDate), 'MMM d, yyyy')}
              </span>
            </div>
            
            <h3 className="text-lg font-semibold text-gray-800 mb-2">{report.wellName}</h3>
            <div className="space-y-2 mb-4">
              <p className="text-gray-600 text-sm">
                <span className="font-medium">Test Type:</span> {report.testType}
              </p>
              <p className="text-gray-600 text-sm">
                <span className="font-medium">Status:</span>{' '}
                <span className={`inline-flex items-center px-2 py-0.5 rounded text-xs font-medium ${
                  report.status === 'completed' 
                    ? 'bg-green-100 text-green-800' 
                    : 'bg-yellow-100 text-yellow-800'
                }`}>
                  {report.status.charAt(0).toUpperCase() + report.status.slice(1)}
                </span>
              </p>
            </div>
            
            <div className="flex items-center justify-between pt-4 border-t border-gray-200">
              <button
                onClick={() => handleViewDetails(report)}
                className="text-blue-600 hover:text-blue-800 transition-colors flex items-center"
              >
                <FileText size={18} className="mr-1" />
                <span className="text-sm">View</span>
              </button>
              <button
                onClick={() => handleExport(report)}
                className="text-blue-600 hover:text-blue-800 transition-colors flex items-center"
              >
                <Download size={18} className="mr-1" />
                <span className="text-sm">Export</span>
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

export default Reports;