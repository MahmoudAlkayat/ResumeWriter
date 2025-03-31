"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { Button } from "@/components/ui/button";
import { Background } from "@/components/ui/background";
import { Alert, AlertTitle } from "@/components/ui/alert";
import LoadingScreen from "@/components/LoadingScreen";

interface EducationEntry {
  degree: string;
  institution: string;
  startDate: string;
  endDate: string;
  gpa?: number;
}

export default function EducationDisplay() {
  const [education, setEducation] = useState<EducationEntry[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const router = useRouter();

  useEffect(() => {
    async function fetchEducation() {
      try {
        const response = await fetch("/api/resumes/education");
        if (!response.ok) throw new Error("Failed to fetch education data");
        const data = await response.json();
        setEducation(data.education || []);
      } catch (err) {
        if (err instanceof Error) {
          setError(err.message);
        } else {
          setError("An unknown error occurred");
        }
      } finally {
        setLoading(false);
      }
    }
    fetchEducation();
  }, []);

  const handleEdit = (edu: EducationEntry) => {
    router.push(
      `/education/edit?degree=${encodeURIComponent(edu.degree)}&institution=${encodeURIComponent(edu.institution)}&startDate=${encodeURIComponent(edu.startDate)}&endDate=${encodeURIComponent(edu.endDate)}&gpa=${edu.gpa ?? ""}`
    );
  };

  if (loading) {
    return <LoadingScreen />;
  }

  return (
    <Background className="relative flex flex-col items-center justify-center min-h-screen text-center p-6">
      <h1 className="text-5xl font-extrabold text-black mb-6 drop-shadow-md">Education History</h1>
      {error ? (
        <Alert variant="destructive" className="max-w-lg">
          <AlertTitle>Error</AlertTitle>
          {error}
        </Alert>
      ) : education.length === 0 ? (
        <p className="text-xl text-gray-800 drop-shadow-sm">No education records found.</p>
      ) : (
        <div className="space-y-6 w-full max-w-2xl">
          {education.map((edu, index) => (
            <div key={index} className="bg-white shadow-lg rounded-2xl p-6 text-left">
              <h2 className="text-2xl font-semibold text-black mb-2">{edu.degree}</h2>
              <p className="text-lg text-gray-700">{edu.institution}</p>
              <p className="text-gray-600">{edu.startDate} - {edu.endDate}</p>
              {edu.gpa !== undefined && <p className="text-gray-700">GPA: {edu.gpa}</p>}
              <Button
                className="bg-blue-600 text-white hover:bg-blue-700 shadow-md mt-4 px-6 py-2"
                onClick={() => handleEdit(edu)}
              >
                Edit
              </Button>
            </div>
          ))}
        </div>
      )}
    </Background>
  );
}
