"use client";

import { useState, useEffect } from "react";
import { Button } from "@/components/ui/button";
import { Background } from "@/components/ui/background";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import {
  Dialog,
  DialogContent,
  DialogHeader,
} from "@/components/ui/dialog";
import { useToast } from "@/contexts/ToastProvider";
import LoadingScreen from "@/components/LoadingScreen";
import { GeneratedResume, JobDescription } from "@/lib/types";
import GeneratedResumeCard from "@/components/GeneratedResumeCard";
import JobDescriptionCard from "@/components/JobDescriptionCard";
import { DialogTitle } from "@radix-ui/react-dialog";
import { VisuallyHidden } from '@radix-ui/react-visually-hidden'

interface JobApplication {
  applicationId: string;
  resumeId: string;
  jobId: string;
  appliedAt: string;
  jobTitle?: string;
  resumeTitle?: string;
  resumeLabel?: string;
}

export default function JobApplicationsPage() {
  const { showError, showSuccess } = useToast();
  const [applications, setApplications] = useState<JobApplication[]>([]);
  const [jobs, setJobs] = useState<JobDescription[]>([]);
  const [resumes, setResumes] = useState<GeneratedResume[]>([]);
  const [selectedJobId, setSelectedJobId] = useState<string>("");
  const [selectedResumeId, setSelectedResumeId] = useState<string>("");
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [dialogContent, setDialogContent] = useState<{
    type: 'job' | 'resume';
    data: JobDescription | GeneratedResume;
  } | null>(null);

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

  const handleCellClick = (type: 'job' | 'resume', id: string) => {
    if (type === 'job') {
      const job = jobs.find(j => {
        return j.jobId == id;
      });
      if (!job) return;
      setDialogContent({ type: 'job', data: job });
      setDialogOpen(true);
    } else if (type === 'resume') {
      const resume = resumes.find(r => {
        return r.resumeId == id;
      });
      if (!resume) return;
      setDialogContent({ type: 'resume', data: resume });
      setDialogOpen(true);
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
                  <JobDescriptionCard
                    key={job.jobId}
                    job={job}
                    isSelected={selectedJobId === job.jobId}
                    onClick={() => setSelectedJobId(job.jobId)}
                  />
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
                  <GeneratedResumeCard
                    key={resume.resumeId}
                    resume={resume}
                    isSelected={selectedResumeId === resume.resumeId}
                    onClick={() => setSelectedResumeId(resume.resumeId)}
                  />
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
          <div className="w-full">
            <Table className="table-fixed">
              <TableHeader>
                <TableRow>
                  <TableHead className="w-[50%]">Job</TableHead>
                  <TableHead className="w-[30%]">Resume</TableHead>
                  <TableHead className="w-[20%]">Applied At</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {applications.map((application) => (
                  <TableRow key={application.applicationId}>
                    <TableCell
                      className="truncate pr-8"
                    >
                      <span className="text-blue-600 dark:text-blue-400 hover:underline cursor-pointer" 
                        onClick={() => handleCellClick('job', application.jobId)}>
                        {application.jobTitle || `JobID: ${application.jobId}`}
                      </span>
                    </TableCell>
                    <TableCell
                      className="truncate"
                    >
                      <span className="text-blue-600 dark:text-blue-400 hover:underline cursor-pointer"
                      onClick={() => handleCellClick('resume', application.resumeId)}
                      >
                        {application.resumeTitle || `GenID: ${application.resumeId}`}
                      </span>
                    </TableCell>
                    <TableCell className="whitespace-nowrap">
                      {new Date(application.appliedAt).toLocaleString("en-US", {
                        dateStyle: "medium",
                        timeStyle: "short",
                      })}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>
        )}
      </div>

      {/* Dialog for showing job/resume details */}
      <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
        <DialogContent className="min-w-4xl overflow-y-auto max-h-[90vh]">
          <VisuallyHidden>
            <DialogTitle/>
          </VisuallyHidden>
          <DialogHeader/>
          {dialogContent && (
            dialogContent.type === 'job' ? (
              <JobDescriptionCard
                job={dialogContent.data as JobDescription}
              />
            ) : (
              <GeneratedResumeCard
                resume={dialogContent.data as GeneratedResume}
              />
            )
          )}
        </DialogContent>
      </Dialog>
    </Background>
  );
} 