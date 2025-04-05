"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { Button } from "@/components/ui/button";
import { Background } from "@/components/ui/background";
import { Alert, AlertTitle } from "@/components/ui/alert";
import LoadingScreen from "@/components/LoadingScreen";
import { Card, CardContent } from "@/components/ui/card";

interface EducationEntry {
  degree: string;
  institution: string;
  startDate: string;
  endDate: string;
  gpa: number;
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
    <Background className="relative flex flex-col items-center justify-start min-h-screen p-8 text-center">
      <h2 className="text-4xl font-extrabold text-black mb-8 drop-shadow-md">Education History</h2>
      <div className="w-full max-w-5xl bg-white shadow-xl rounded-2xl p-10 border border-gray-200">
        {error ? (
          <div className="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 max-w-lg mx-auto rounded-lg shadow-md">
            <AlertTitle className="font-bold">Error</AlertTitle>
            <p>{error}</p>
          </div>
        ) : education.length === 0 ? (
          <p className="text-xl text-gray-800 drop-shadow-sm text-center">No education records found.</p>
        ) : (
          <div className="space-y-8">
            {education.map((edu, index) => (
              <Card key={index} className="p-8 shadow-md rounded-xl bg-gray-50 border border-gray-300">
                <CardContent className="flex flex-col gap-3">
                  <h3 className="text-2xl font-bold text-black">{edu.degree}</h3>
                  <p className="text-lg text-gray-700 font-semibold">{edu.institution}</p>
                  <p className="text-md text-gray-500 italic">
                    {edu.startDate} - {edu.endDate}
                  </p>
                  {edu.gpa !== undefined && <p className="text-gray-700">GPA: {edu.gpa}</p>}
                  <div className="mt-4 flex justify-end">
                    <Button className="bg-blue-600 text-white hover:bg-blue-700 shadow-md px-5 py-2 text-lg flex items-center" onClick={() => handleEdit(edu)}>
                      Edit
                    </Button>
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