import { useState, useRef } from 'react';
import { profile as profileApi } from '../services/api';
import LoadingScreen from '../components/common/LoadingScreen';
import { Profile } from '../types';

const ProfileAssistant = () => {
  const [loading, setLoading] = useState(false);
  const [profile, setProfile] = useState<Profile | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleFileUpload = async (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (!file) return;

    try {
      setLoading(true);
      const response = await profileApi.generate(file);
      setProfile(response.data);
    } catch (error) {
      console.error('Error generating profile:', error);
      alert('Failed to generate profile. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleProfileUpdate = async (updatedProfile: Profile) => {
    try {
      setLoading(true);
      const response = await profileApi.update(updatedProfile);
      setProfile(response.data.profile);
    } catch (error) {
      console.error('Error updating profile:', error);
      alert('Failed to update profile. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <LoadingScreen />;
  }

  return (
    <div className="space-y-8">
      {/* Header */}
      <div className="bg-white rounded-lg shadow-lg p-8">
        <h1 className="text-3xl font-bold text-gray-800 mb-4">LinkedIn Profile Assistant</h1>
        <p className="text-gray-600">
          Generate a professional LinkedIn profile from your resume using AI.
        </p>
      </div>

      {!profile ? (
        // Upload Section
        <div className="bg-white rounded-lg shadow-lg p-8">
          <div className="text-center mb-6">
            <i className="fas fa-user-tie text-5xl text-primary mb-4"></i>
            <h2 className="text-2xl font-bold text-gray-800 mb-2">Upload Resume</h2>
            <p className="text-gray-600">Upload your resume to generate a LinkedIn profile</p>
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
      ) : (
        // Profile Display Section
        <div className="space-y-6">
          {/* Profile Strength */}
          <div className="bg-white rounded-lg shadow-lg p-6">
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-xl font-bold text-gray-800">Profile Strength</h2>
              <span className="text-2xl font-bold text-primary">
                {profile.profileStrength}%
              </span>
            </div>
            <div className="w-full bg-gray-200 rounded-full h-2.5">
              <div
                className="bg-primary h-2.5 rounded-full"
                style={{ width: `${profile.profileStrength}%` }}
              ></div>
            </div>
          </div>

          {/* Basic Info */}
          <div className="bg-white rounded-lg shadow-lg p-8">
            <div className="flex items-start gap-6">
              <img
                src={profile.profilePicture || 'https://via.placeholder.com/150'}
                alt="Profile"
                className="w-32 h-32 rounded-full"
              />
              <div className="flex-1">
                <h2 className="text-2xl font-bold text-gray-800 mb-2">{profile.name}</h2>
                <p className="text-lg text-gray-600 mb-2">{profile.headline}</p>
                <p className="text-gray-500">
                  <i className="fas fa-map-marker-alt mr-2"></i>
                  {profile.location}
                </p>
              </div>
            </div>
          </div>

          {/* About */}
          <div className="bg-white rounded-lg shadow-lg p-8">
            <h3 className="text-xl font-bold text-gray-800 mb-4">About</h3>
            <p className="text-gray-600 whitespace-pre-line">{profile.about}</p>
          </div>

          {/* Experience */}
          <div className="bg-white rounded-lg shadow-lg p-8">
            <h3 className="text-xl font-bold text-gray-800 mb-4">Experience</h3>
            <div className="space-y-6">
              {profile.experience.map((exp, index) => (
                <div key={index} className="border-b last:border-0 pb-6 last:pb-0">
                  <h4 className="text-lg font-semibold text-gray-800">{exp.title}</h4>
                  <p className="text-gray-600">{exp.company}</p>
                  <p className="text-gray-500 text-sm">{exp.duration}</p>
                  <p className="text-gray-600 mt-2">{exp.description}</p>
                </div>
              ))}
            </div>
          </div>

          {/* Education */}
          <div className="bg-white rounded-lg shadow-lg p-8">
            <h3 className="text-xl font-bold text-gray-800 mb-4">Education</h3>
            <div className="space-y-6">
              {profile.education.map((edu, index) => (
                <div key={index} className="border-b last:border-0 pb-6 last:pb-0">
                  <h4 className="text-lg font-semibold text-gray-800">{edu.degree}</h4>
                  <p className="text-gray-600">{edu.school}</p>
                  <p className="text-gray-500 text-sm">{edu.duration}</p>
                  {edu.description && (
                    <p className="text-gray-600 mt-2">{edu.description}</p>
                  )}
                </div>
              ))}
            </div>
          </div>

          {/* Skills */}
          <div className="bg-white rounded-lg shadow-lg p-8">
            <h3 className="text-xl font-bold text-gray-800 mb-4">Skills</h3>
            <div className="flex flex-wrap gap-2">
              {profile.skills.map((skill, index) => (
                <span key={index} className="skill-badge">
                  {skill}
                </span>
              ))}
            </div>
          </div>

          {/* Projects */}
          {profile.projects.length > 0 && (
            <div className="bg-white rounded-lg shadow-lg p-8">
              <h3 className="text-xl font-bold text-gray-800 mb-4">Projects</h3>
              <div className="space-y-6">
                {profile.projects.map((project, index) => (
                  <div key={index} className="border-b last:border-0 pb-6 last:pb-0">
                    <h4 className="text-lg font-semibold text-gray-800">{project.name}</h4>
                    <p className="text-gray-500 text-sm">{project.duration}</p>
                    <p className="text-gray-600 mt-2">{project.description}</p>
                    <p className="text-gray-600 mt-2">
                      <span className="font-medium">Technologies:</span> {project.technologies}
                    </p>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Certifications */}
          {profile.certifications.length > 0 && (
            <div className="bg-white rounded-lg shadow-lg p-8">
              <h3 className="text-xl font-bold text-gray-800 mb-4">Certifications</h3>
              <div className="space-y-6">
                {profile.certifications.map((cert, index) => (
                  <div key={index} className="border-b last:border-0 pb-6 last:pb-0">
                    <h4 className="text-lg font-semibold text-gray-800">{cert.name}</h4>
                    <p className="text-gray-600">{cert.issuer}</p>
                    <p className="text-gray-500 text-sm">{cert.date}</p>
                    {cert.description && (
                      <p className="text-gray-600 mt-2">{cert.description}</p>
                    )}
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Recommendations */}
          {profile.recommendations.length > 0 && (
            <div className="bg-white rounded-lg shadow-lg p-8">
              <h3 className="text-xl font-bold text-gray-800 mb-4">Recommendations</h3>
              <div className="space-y-6">
                {profile.recommendations.map((rec, index) => (
                  <div key={index} className="recommendation">
                    <p className="text-gray-600 mb-4">{rec.text}</p>
                    <p className="text-gray-700 font-medium">{rec.recommender}</p>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Actions */}
          <div className="flex justify-end gap-4">
            <button
              onClick={() => fileInputRef.current?.click()}
              className="btn-outline-primary"
            >
              <i className="fas fa-redo mr-2"></i>
              Generate New
            </button>
            <button
              onClick={() => handleProfileUpdate(profile)}
              className="btn-primary"
            >
              <i className="fas fa-save mr-2"></i>
              Update Profile
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default ProfileAssistant;
