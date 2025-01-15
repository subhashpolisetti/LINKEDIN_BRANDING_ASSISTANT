import { useEffect, useState } from 'react';
import { jobs as jobsApi } from '../services/api';
import LoadingScreen from '../components/common/LoadingScreen';
import { Job } from '../types';

const Jobs = () => {
  const [jobs, setJobs] = useState<Job[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [filters, setFilters] = useState({
    location: '',
    employmentType: '',
    experienceLevel: ''
  });

  useEffect(() => {
    fetchJobs();
  }, []);

  const fetchJobs = async () => {
    try {
      setLoading(true);
      const response = await jobsApi.getAll();
      setJobs(response.data);
    } catch (error) {
      console.error('Error fetching jobs:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleRefresh = async () => {
    try {
      setLoading(true);
      const response = await jobsApi.refresh();
      setJobs(response.data.jobs);
    } catch (error) {
      console.error('Error refreshing jobs:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = async () => {
    if (!searchTerm) {
      fetchJobs();
      return;
    }

    try {
      setLoading(true);
      const response = await jobsApi.search(searchTerm);
      setJobs(response.data);
    } catch (error) {
      console.error('Error searching jobs:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleFilter = async () => {
    try {
      setLoading(true);
      const response = await jobsApi.filter(filters);
      setJobs(response.data);
    } catch (error) {
      console.error('Error filtering jobs:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <LoadingScreen />;
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex justify-between items-center">
        <h1 className="text-3xl font-bold text-gray-800">Available Jobs</h1>
        <button onClick={handleRefresh} className="btn-primary">
          <i className="fas fa-sync-alt mr-2"></i>
          Refresh Jobs
        </button>
      </div>

      {/* Search and Filters */}
      <div className="bg-white rounded-lg shadow-lg p-6">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          <div>
            <input
              type="text"
              placeholder="Search jobs..."
              className="input-field"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
            />
          </div>
          <div>
            <input
              type="text"
              placeholder="Location"
              className="input-field"
              value={filters.location}
              onChange={(e) => setFilters({ ...filters, location: e.target.value })}
            />
          </div>
          <div>
            <select
              className="input-field"
              value={filters.employmentType}
              onChange={(e) => setFilters({ ...filters, employmentType: e.target.value })}
            >
              <option value="">Employment Type</option>
              <option value="Full-time">Full-time</option>
              <option value="Part-time">Part-time</option>
              <option value="Contract">Contract</option>
              <option value="Internship">Internship</option>
            </select>
          </div>
          <div>
            <select
              className="input-field"
              value={filters.experienceLevel}
              onChange={(e) => setFilters({ ...filters, experienceLevel: e.target.value })}
            >
              <option value="">Experience Level</option>
              <option value="Entry">Entry Level</option>
              <option value="Mid">Mid Level</option>
              <option value="Senior">Senior Level</option>
              <option value="Executive">Executive</option>
            </select>
          </div>
        </div>
        <div className="mt-4 flex justify-end">
          <button onClick={handleFilter} className="btn-primary">
            <i className="fas fa-filter mr-2"></i>
            Apply Filters
          </button>
        </div>
      </div>

      {/* Jobs Grid */}
      {jobs.length > 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {jobs.map((job) => (
            <div key={job.id} className="job-card">
              <h3 className="job-title">{job.title}</h3>
              <p className="company-name">
                <i className="fas fa-building mr-2"></i>
                {job.companyName}
              </p>
              <div className="flex justify-between items-center text-sm text-gray-500 mb-4">
                <span>
                  <i className="fas fa-map-marker-alt mr-1"></i>
                  {job.location}
                </span>
                <span>
                  <i className="fas fa-clock mr-1"></i>
                  {new Date(job.listedAt).toLocaleDateString()}
                </span>
              </div>
              <div className="mb-4">
                <div className="text-sm font-medium text-gray-500 mb-2">Required Skills:</div>
                <div className="flex flex-wrap gap-2">
                  {job.skills.slice(0, 3).map((skill, index) => (
                    <span key={index} className="skill-badge">
                      {skill}
                    </span>
                  ))}
                  {job.skills.length > 3 && (
                    <span className="skill-badge">+{job.skills.length - 3} more</span>
                  )}
                </div>
              </div>
              <a href={job.jobPostingUrl} target="_blank" rel="noopener noreferrer" 
                 className="view-job-btn">
                <i className="fas fa-external-link-alt mr-2"></i>
                View Job
              </a>
            </div>
          ))}
        </div>
      ) : (
        <div className="bg-white rounded-lg shadow-lg p-8 text-center">
          <i className="fas fa-briefcase text-5xl text-gray-400 mb-4"></i>
          <p className="text-gray-600 text-lg mb-4">No jobs available at the moment.</p>
          <p className="text-gray-500">Check back later for new opportunities!</p>
        </div>
      )}
    </div>
  );
};

export default Jobs;
