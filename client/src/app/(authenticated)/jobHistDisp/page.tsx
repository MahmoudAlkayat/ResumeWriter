"use client";

import { useEffect, useState } from "react";
import { Button } from "@/components/ui/button";
import { Background } from "@/components/ui/background";
import { Card, CardContent } from "@/components/ui/card";
import { useAuth } from "@/hooks/auth";
import { useToast } from "@/contexts/ToastProvider";
import { Trash, ExternalLink } from "lucide-react";
import LoadingScreen from "@/components/LoadingScreen";

interface JobApplication {
  applicationId: string;
  resumeId: string;
  resumeLabel?: string;
  jobId: string;
  jobSummary?: string;
  appliedAt: string;
}

interface AuthUser {
  id: string;
  // Add other user properties you expect
  token?: string; // Using 'token' instead of 'accessToken'
}

export default function ApplicationHistory() {
  const { user } = useAuth() as { user: AuthUser | null };
  const { showError, showSuccess } = useToast();
  const [isLoading, setIsLoading] = useState(true);
  const [applications, setApplications] = useState<JobApplication[]>([]);

  async function fetchApplications() {
    if (!user?.id) return;
    
    try {
      setIsLoading(true);
      const response = await fetch(`/api/user/job-applications`, {
        credentials: "include",
        headers: {
          Authorization: `Bearer ${user.token}`, // Changed from accessToken to token
        },
      });

      if (!response.ok) {
        const errText = await response.text();
        throw new Error(errText || "Failed to fetch applications");
      }

      const data = await response.json();
      setApplications(data.applications || []);
    } catch (err) {
      if (err instanceof Error) showError(err.message);
      else showError("An unknown error occurred");
    } finally {
      setIsLoading(false);
    }
  }

  useEffect(() => {
    fetchApplications();
  }, [user?.id]);

  const handleDelete = async (applicationId: string) => {
    if (!user?.id) {
      showError("Not authenticated");
      return;
    }

    try {
      const res = await fetch(
        `/api/user/job-applications/${applicationId}`,
        {
          method: "DELETE",
          credentials: "include",
          headers: {
            Authorization: `Bearer ${user.token}`, // Changed from accessToken to token
          },
        }
      );
      
      if (!res.ok) {
        const errText = await res.text();
        throw new Error(errText || "Failed to delete application record");
      }
      
      showSuccess("Application record deleted successfully");
      fetchApplications();
    } catch (err) {
      if (err instanceof Error) {
        showError(err.message);
      } else {
        showError("An unknown error occurred");
      }
    }
  };

  if (isLoading) {
    return <LoadingScreen />;
  }

  return (
    <Background className="relative flex flex-col items-center justify-start min-h-screen p-8 text-center">
      <h2 className="text-4xl font-bold text-primary mb-8 drop-shadow-md">
        Application History
      </h2>

      <div className="w-full max-w-5xl bg-white shadow-xl rounded-2xl p-10 border border-gray-200 mb-8 dark:bg-neutral-900 dark:border-neutral-800">
        {applications.length === 0 ? (
          <p className="text-xl text-foreground drop-shadow-sm text-center">
            No job applications found.
          </p>
        ) : (
          <div className="space-y-6">
            {applications.map((application) => (
              <Card
                key={application.applicationId}
                className="p-6 shadow-md rounded-xl bg-gray-50 border border-gray-300 dark:bg-neutral-800 dark:border-neutral-700"
              >
                <CardContent className="flex flex-col gap-4">
                  <div className="flex justify-between items-start">
                    <div className="flex-1">
                      <h3 className="text-2xl font-bold text-primary">
                        {application.resumeLabel || `Resume ${application.resumeId}`}
                      </h3>
                      <p className="text-lg text-gray-700 dark:text-white font-semibold mt-1">
                        {application.jobSummary || `Job ${application.jobId}`}
                      </p>
                    </div>
                    <div className="flex gap-2">
                      <Button
                        variant="outline"
                        className="text-blue-600 border-blue-600 hover:bg-blue-50 dark:hover:bg-blue-900/30"
                        onClick={() => {
                          // Add functionality to view resume or job details
                          window.open(`/resumes/${application.resumeId}`, '_blank');
                        }}
                      >
                        <ExternalLink size={16} className="mr-2" />
                        View Resume
                      </Button>
                      <Button
                        onClick={() => handleDelete(application.applicationId)}
                        className="bg-red-600 text-white hover:bg-red-700"
                      >
                        <Trash size={16} />
                      </Button>
                    </div>
                  </div>

                  <div className="flex items-center justify-between mt-2">
                    <span className="text-sm text-gray-500 dark:text-muted-foreground">
                      Applied on:{" "}
                      {new Date(application.appliedAt).toLocaleDateString("en-US", {
                        year: "numeric",
                        month: "long",
                        day: "numeric",
                        hour: "2-digit",
                        minute: "2-digit",
                      })}
                    </span>
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