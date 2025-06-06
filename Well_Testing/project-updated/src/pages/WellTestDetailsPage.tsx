import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { Loader2 } from 'lucide-react';
import { loadWellTest } from '../services/wellTestService';
import type { WellTest } from '../services/wellTestService';
import WellTestDetails from '../components/WellTestDetails';

function WellTestDetailsPage() {
  const { id } = useParams<{ id: string }>();
  const [wellTest, setWellTest] = useState<WellTest | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    async function fetchWellTest() {
      if (!id) return;
      
      try {
        const data = await loadWellTest(id);
        setWellTest(data);
        setError(null);
      } catch (err) {
        setError('Failed to load well test details. Please try again later.');
        console.error('Error fetching well test:', err);
      } finally {
        setLoading(false);
      }
    }

    fetchWellTest();
  }, [id]);

  if (loading) {
    return (
      <div className="container mx-auto px-4 py-6">
        <div className="flex items-center justify-center min-h-[400px]">
          <Loader2 className="h-8 w-8 animate-spin text-primary-600" />
          <span className="ml-2 text-gray-600">Loading well test details...</span>
        </div>
      </div>
    );
  }

  if (error || !wellTest) {
    return (
      <div className="container mx-auto px-4 py-6">
        <div className="bg-red-50 border border-red-200 rounded-lg p-4 text-red-700">
          <p>{error || 'Well test not found'}</p>
        </div>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-6">
      <WellTestDetails wellTest={wellTest} />
    </div>
  );
}

export default WellTestDetailsPage;