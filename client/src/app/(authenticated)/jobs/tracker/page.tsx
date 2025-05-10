"use client";

import { useState, useEffect, useRef } from "react";
import { Button } from "@/components/ui/button";
import { Background } from "@/components/ui/background";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  CardFooter,
} from "@/components/ui/card";
import { useToast } from "@/contexts/ToastProvider";
import LoadingScreen from "@/components/LoadingScreen";

interface JobApplication {
  applicationId: string;
  resumeId: string;
  jobId: string;
  appliedAt: string;
  jobTitle?: string;
  resumeLabel?: string;
}

interface JobDescription {
    jobId: string;
    title?: string;
    text: string;
    submittedAt: string;
  }

interface Resume {
  resumeId: string;
  content: string;
  createdAt: string;
  updatedAt: string;
  jobId: string;
  jobDescriptionTitle: string | null;
}

export default function JobApplicationsPage() {
  const { showError, showSuccess } = useToast();
  const [applications, setApplications] = useState<JobApplication[]>([]);
  const [jobs, setJobs] = useState<JobDescription[]>([]);
  const [resumes, setResumes] = useState<Resume[]>([]);
  const [selectedJobId, setSelectedJobId] = useState<string>("");
  const [selectedResumeId, setSelectedResumeId] = useState<string>("");
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [expandedJobs, setExpandedJobs] = useState<Set<string>>(new Set());
  const [expandedResumes, setExpandedResumes] = useState<Set<string>>(new Set());
  const jobRefs = useRef<Record<string, HTMLParagraphElement | null>>({});
  const resumeRefs = useRef<Record<string, HTMLPreElement | null>>({});
  const [overflowingJobs, setOverflowingJobs] = useState<Set<string>>(new Set());
  const [overflowingResumes, setOverflowingResumes] = useState<Set<string>>(new Set());

  // Fetch applications, jobs, and resumes
  useEffect(() => {
    const fetchData = async () => {
      try {
        const [applicationsRes, jobsRes, resumesRes] = await Promise.all([
          fetch("http://localhost:8080/api/user/job-applications", {
            credentials: "include",
          }),
          fetch("http://localhost:8080/api/jobs/history", {
            credentials: "include",
          }),
          fetch("http://localhost:8080/api/resumes/generate/history", {
            credentials: "include",
          }),
        ]);

        if (!applicationsRes.ok) throw new Error("Failed to fetch applications");
        if (!jobsRes.ok) throw new Error("Failed to fetch jobs");
        if (!resumesRes.ok) throw new Error("Failed to fetch resumes");

        const [applicationsData, jobsData, resumesData] = await Promise.all([
          applicationsRes.json(),
          jobsRes.json(),
          resumesRes.json(),
        ]);

        setApplications(applicationsData.applications || []);
        setJobs(jobsData || []);
        setResumes(resumesData || []);
      } catch (err) {
        showError(err instanceof Error ? err.message : "Failed to load data");
      } finally {
        setIsLoading(false);
      }
    };

    fetchData();
  }, []);

  useEffect(() => {
    const newOverflowingJobs = new Set<string>();
    jobs.forEach((job) => {
      const el = jobRefs.current[job.jobId];
      if (el && el.scrollHeight > el.clientHeight) {
        newOverflowingJobs.add(job.jobId);
      }
    });
    setOverflowingJobs(newOverflowingJobs);
  }, [jobs]);

  useEffect(() => {
    const newOverflowingResumes = new Set<string>();
    resumes.forEach((resume) => {
      const el = resumeRefs.current[resume.resumeId];
      if (el && el.scrollHeight > el.clientHeight) {
        newOverflowingResumes.add(resume.resumeId);
      }
    });
    setOverflowingResumes(newOverflowingResumes);
  }, [resumes]);

  const handleCreateApplication = async () => {
    if (!selectedJobId || !selectedResumeId) {
      showError("Please select both a job and a resume");
      return;
    }

    setIsSubmitting(true);
    try {
      const response = await fetch("http://localhost:8080/api/user/job-applications", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        credentials: "include",
        body: JSON.stringify({
          jobId: selectedJobId,
          resumeId: selectedResumeId,
        }),
      });

      if (!response.ok) {
        const error = await response.json();
        throw new Error(error.error || "Failed to create application");
      }

      const data = await response.json();
      showSuccess("Application recorded successfully!");
      
      // Refresh applications list
      const applicationsRes = await fetch("http://localhost:8080/api/user/job-applications", {
        credentials: "include",
      });
      if (applicationsRes.ok) {
        const applicationsData = await applicationsRes.json();
        setApplications(applicationsData.applications || []);
      }

      // Reset selections
      setSelectedJobId("");
      setSelectedResumeId("");
    } catch (err) {
      showError(err instanceof Error ? err.message : "Failed to create application");
    } finally {
      setIsSubmitting(false);
    }
  };

  if (isLoading) {
    return <LoadingScreen />;
  }

  return (
    <Background className="relative flex flex-col items-center min-h-screen p-8">
      <h1 className="text-4xl font-bold text-foreground drop-shadow-md text-center mb-8">
        Job Application Tracker
      </h1>

      {/* Create New Application Section */}
      <div className="w-full max-w-7xl bg-white shadow-xl rounded-2xl p-10 border border-gray-200 mb-8 
        dark:bg-neutral-900 dark:border-neutral-800">
        <h2 className="text-2xl font-semibold text-foreground mb-6 text-center">
          Record New Application
        </h2>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
          {/* Job Descriptions Section */}
          <div className="space-y-4">
            <h3 className="text-xl font-semibold text-foreground mb-4 text-center">
              Select Job Description
            </h3>
            <div className="max-h-[400px] overflow-y-auto space-y-4 pr-2">
              {jobs.length === 0 ? (
                <p className="text-muted-foreground text-center py-4">
                  No job descriptions found. Please submit a job description first.
                </p>
              ) : (
                jobs.map((job) => (
                  <Card
                    key={job.jobId}
                    className={`p-4 shadow-md rounded-xl transition-all duration-200 ${
                      selectedJobId === job.jobId
                        ? "border-2 border-blue-500 bg-blue-50 dark:bg-blue-900/20"
                        : "border border-gray-300 dark:border-neutral-700"
                    }`}
                    onClick={() => setSelectedJobId(job.jobId)}
                  >
                    <CardHeader className="-mb-4">
                      <CardTitle className="line-clamp-1 text-lg">
                        {job.title ? job.title : `JobID: ${job.jobId}`}
                      </CardTitle>
                    </CardHeader>
                    <CardContent>
                      <p
                        ref={(el) => {
                          jobRefs.current[job.jobId] = el;
                        }}
                        className={`text-sm text-gray-700 dark:text-gray-300 whitespace-pre-wrap break-words ${
                          !expandedJobs.has(job.jobId) ? "line-clamp-4" : ""
                        }`}
                      >
                        {job.text}
                      </p>
                      {overflowingJobs.has(job.jobId) && (
                        <Button
                          variant="link"
                          className="p-0 h-auto text-sm mt-2"
                          onClick={(e) => {
                            e.stopPropagation();
                            const newExpanded = new Set(expandedJobs);
                            if (expandedJobs.has(job.jobId)) {
                              newExpanded.delete(job.jobId);
                            } else {
                              newExpanded.add(job.jobId);
                            }
                            setExpandedJobs(newExpanded);
                          }}
                        >
                          {expandedJobs.has(job.jobId) ? "Show less" : "Read more"}
                        </Button>
                      )}
                    </CardContent>
                    <CardFooter className="-mt-2">
                      <p className="text-sm text-muted-foreground">
                        Submitted: {new Date(job.submittedAt).toLocaleString()}
                      </p>
                    </CardFooter>
                  </Card>
                ))
              )}
            </div>
          </div>

          {/* Resumes Section */}
          <div className="space-y-4">
            <h3 className="text-xl font-semibold text-foreground mb-4 text-center">
              Select Resume
            </h3>
            <div className="max-h-[400px] overflow-y-auto space-y-4 pr-2">
              {resumes.length === 0 ? (
                <p className="text-muted-foreground text-center py-4">
                  No generated resumes found. Please generate a resume first.
                </p>
              ) : (
                resumes.map((resume) => (
                  <Card
                    key={resume.resumeId}
                    className={`p-4 shadow-md rounded-xl transition-all duration-200 ${
                      selectedResumeId === resume.resumeId
                        ? "border-2 border-blue-500 bg-blue-50 dark:bg-blue-900/20"
                        : "border border-gray-300 dark:border-neutral-700"
                    }`}
                    onClick={() => setSelectedResumeId(resume.resumeId)}
                  >
                    <CardHeader className="-mb-4">
                      <CardTitle className="line-clamp-1 text-lg">
                        {resume.jobDescriptionTitle || `Resume for Job ID: ${resume.jobId}`}
                      </CardTitle>
                    </CardHeader>
                    <CardContent>
                      <pre
                        ref={(el) => {
                          resumeRefs.current[resume.resumeId] = el;
                        }}
                        className={`text-sm text-gray-700 dark:text-gray-300 whitespace-pre-wrap break-words ${
                          !expandedResumes.has(resume.resumeId) ? "line-clamp-4" : ""
                        }`}
                      >
                        {(() => {
                          try {
                            return JSON.stringify(JSON.parse(resume.content), null, 2);
                          } catch {
                            return resume.content;
                          }
                        })()}
                      </pre>
                      {overflowingResumes.has(resume.resumeId) && (
                        <Button
                          variant="link"
                          className="p-0 h-auto text-sm mt-2"
                          onClick={(e) => {
                            e.stopPropagation();
                            const newExpanded = new Set(expandedResumes);
                            if (expandedResumes.has(resume.resumeId)) {
                              newExpanded.delete(resume.resumeId);
                            } else {
                              newExpanded.add(resume.resumeId);
                            }
                            setExpandedResumes(newExpanded);
                          }}
                        >
                          {expandedResumes.has(resume.resumeId) ? "Show less" : "Read more"}
                        </Button>
                      )}
                    </CardContent>
                    <CardFooter className="-mt-2">
                      <p className="text-sm text-muted-foreground">
                        Last Updated: {new Date(resume.updatedAt).toLocaleString()}
                      </p>
                    </CardFooter>
                  </Card>
                ))
              )}
            </div>
          </div>
        </div>

        <div className="flex justify-center mt-8">
          <Button
            onClick={handleCreateApplication}
            disabled={isSubmitting || !selectedJobId || !selectedResumeId}
            className={`${
              isSubmitting || !selectedJobId || !selectedResumeId
                ? "bg-blue-400"
                : "bg-blue-600 hover:bg-blue-700"
            }`}
          >
            {isSubmitting ? "Recording..." : "Record Application"}
          </Button>
        </div>
      </div>

      {/* Application History Section */}
      <div className="w-full max-w-7xl bg-white shadow-xl rounded-2xl p-10 border border-gray-200 
        dark:bg-neutral-900 dark:border-neutral-800">
        <h2 className="text-2xl font-semibold text-foreground mb-6 text-center">
          Application History
        </h2>
        {applications.length === 0 ? (
          <p className="text-xl text-foreground mb-6 drop-shadow-sm text-center">
            No job applications submitted yet.
          </p>
        ) : (
          <div className="space-y-6">
            {applications.map((application) => (
              <Card
                key={application.applicationId}
                className="p-4 shadow-md rounded-xl bg-gray-50 border border-gray-300 
                  dark:bg-neutral-800 dark:border-neutral-700"
              >
                <CardHeader className="pb-2">
                  <CardTitle className="text-lg truncate">
                    {application.resumeLabel || `Resume ${application.resumeId}`}
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="space-y-2">
                    <p className="text-sm text-gray-600 dark:text-gray-400 break-words">
                      <span className="font-medium">Job:</span>{" "}
                      {application.jobTitle || `Job ${application.jobId}`}
                    </p>
                    <p className="text-sm text-gray-600 dark:text-gray-400">
                      <span className="font-medium">Applied:</span>{" "}
                      {new Date(application.appliedAt).toLocaleString("en-US", {
                        dateStyle: "medium",
                        timeStyle: "short",
                      })}
                    </p>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        )}
      </div>
    </Background>
  );
} 