import { useState, useRef } from 'react';
import { resume as resumeApi } from '../services/api';
import LoadingScreen from '../components/common/LoadingScreen';
import { JobAnalysis } from '../types';

const ResumeAnalysis = () => {
  const [loading, setLoading] = useState(false);
  const [resumeId, setResumeId] = useState<string | null>(null);
  const [jobDescription, setJobDescription] = useState('');
  const [analysis, setAnalysis] = useState<JobAnalysis | null>(null);
  const [tailoredPoints, setTailoredPoints] = useState<string[]>([]);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleFileUpload = async (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (!file) return;

    try {
      setLoading(true);
      const formData = new FormData();
      formData.append('resume', file);

      const response = await resumeApi.upload(file);
      setResumeId(response.data.resume_id);
    } catch (error) {
      console.error('Error uploading resume:', error);
      alert('Failed to upload resume. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleAnalyze = async () => {
    if (!resumeId || !jobDescription.trim()) {
      alert('Please upload a resume and enter a job description.');
      return;
    }

    try {
      setLoading(true);
      const response = await resumeApi.analyze(resumeId, jobDescription);
      setAnalysis(response.data);
    } catch (error) {
      console.error('Error analyzing resume:', error);
      alert('Failed to analyze resume. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleGeneratePoints = async () => {
    if (!resumeId || !jobDescription.trim() || !analysis?.matchedKeywords) {
      alert('Please analyze the resume first.');
      return;
    }

    try {
      setLoading(true);
      const response = await resumeApi.tailor(resumeId, jobDescription, analysis.matchedKeywords);
      setTailoredPoints(response.data.tailored_points);
    } catch (error) {
      console.error('Error generating points:', error);
      alert('Failed to generate tailored points. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const copyPoints = () => {
    const text = tailoredPoints.join('\n\n');
    navigator.clipboard.writeText(text)
      .then(() => alert('Points copied to clipboard!'))
      .catch(() => alert('Failed to copy points. Please try selecting and copying manually.'));
  };

  if (loading) {
    return <LoadingScreen />;
  }

  return (
    <div className="space-y-8">
      {/* Header */}
      <div className="bg-white rounded-lg shadow-lg p-8">
        <h1 className="text-3xl font-bold text-gray-800 mb-4">Resume Analysis</h1>
        <p className="text-gray-600">
          Upload your resume and analyze it against job descriptions to see how well you match.
        </p>
      </div>

      {/* Upload Section */}
      <div className="bg-white rounded-lg shadow-lg p-8">
        <div className="text-center mb-6">
          <i className="fas fa-file-upload text-5xl text-primary mb-4"></i>
          <h2 className="text-2xl font-bold text-gray-800 mb-2">Upload Resume</h2>
          <p className="text-gray-600">Upload your resume in PDF format</p>
        </div>

        <div className="upload-area" onClick={() => fileInputRef.current?.click()}>
          <input
            ref={fileInputRef}
            type="file"
            accept=".pdf"
            onChange={handleFileUpload}
            className="hidden"
          />
          <div className="text-center">
            <i className="fas fa-cloud-upload-alt text-3xl text-primary mb-2"></i>
            <p className="text-gray-600">Click to upload or drag and drop</p>
            <p className="text-sm text-gray-500">PDF files only</p>
          </div>
        </div>
      </div>

      {/* Analysis Section */}
      {resumeId && (
        <div className="bg-white rounded-lg shadow-lg p-8">
          <h2 className="text-2xl font-bold text-gray-800 mb-4">Analyze Job Match</h2>
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Job Description
              </label>
              <textarea
                className="input-field h-40"
                placeholder="Paste the job description here..."
                value={jobDescription}
                onChange={(e) => setJobDescription(e.target.value)}
              />
            </div>
            <div className="flex gap-4">
              <button onClick={handleAnalyze} className="btn-primary flex-1">
                <i className="fas fa-search mr-2"></i>
                Analyze Match
              </button>
              <button
                onClick={handleGeneratePoints}
                className={`btn-primary flex-1 ${!analysis ? 'opacity-50 cursor-not-allowed' : ''}`}
                disabled={!analysis}
              >
                <i className="fas fa-magic mr-2"></i>
                Generate Points
              </button>
            </div>
          </div>

          {/* Analysis Results */}
          {analysis && (
            <div className="mt-8 space-y-6">
              <div className="bg-primary/10 rounded-lg p-6 border-2 border-primary">
                <div className="text-center">
                  <div className="text-4xl font-bold text-primary mb-2">
                    {analysis.matchScore}
                  </div>
                  <div className="text-gray-600">Match Score</div>
                </div>
              </div>

              <div>
                <h3 className="text-xl font-bold text-gray-800 mb-4">Matched Keywords</h3>
                <div className="flex flex-wrap gap-2">
                  {analysis.matchedKeywords.map((keyword, index) => (
                    <span key={index} className="skill-badge">
                      {keyword}
                    </span>
                  ))}
                </div>
              </div>

              {analysis.missingKeywords.length > 0 && (
                <div>
                  <h3 className="text-xl font-bold text-gray-800 mb-4">Missing Keywords</h3>
                  <div className="flex flex-wrap gap-2">
                    {analysis.missingKeywords.map((keyword, index) => (
                      <span key={index} className="skill-badge bg-error/10 text-error">
                        {keyword}
                      </span>
                    ))}
                  </div>
                </div>
              )}
            </div>
          )}

          {/* Tailored Points */}
          {tailoredPoints.length > 0 && (
            <div className="mt-8">
              <div className="flex justify-between items-center mb-4">
                <h3 className="text-xl font-bold text-gray-800">Tailored Resume Points</h3>
                <button onClick={copyPoints} className="btn-outline-primary">
                  <i className="fas fa-copy mr-2"></i>
                  Copy All
                </button>
              </div>
              <div className="space-y-4">
                {tailoredPoints.map((point, index) => (
                  <div key={index} className="bullet-point">
                    {point}
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default ResumeAnalysis;
