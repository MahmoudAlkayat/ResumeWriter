"use client";

import { useEffect, useState } from "react";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Background } from "@/components/ui/background";
import LoadingScreen from "@/components/LoadingScreen";
import { Pencil } from "lucide-react";

interface Job {
  title: string;
  company: string;
  startDate: string;
  endDate: string;
  responsibilities: string;
}

export default function CareerHistoryDisplay() {
  const [careerHistory, setCareerHistory] = useState<Job[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetch("/api/resumes/history")
      .then((response) => response.json())
      .then((data) => {
        setCareerHistory(data.jobs || []);
        setLoading(false);
      })
      .catch((error) => {
        console.error("Error fetching career history:", error);
        setLoading(false);
      });
  }, []);

  if (loading) {
    return <LoadingScreen />;
  }

  return (
    <Background className="relative flex flex-col items-center justify-start min-h-screen p-8 text-center">
      <h2 className="text-4xl font-extrabold text-black mb-8 drop-shadow-md">Career History</h2>
      <div className="w-full max-w-5xl bg-white shadow-xl rounded-2xl p-10 border border-gray-200">
        {careerHistory.length === 0 ? (
          <p className="text-xl text-gray-800 drop-shadow-sm text-center">No career history available.</p>
        ) : (
          <div className="space-y-8">
            {careerHistory.map((job, index) => (
              <Card key={index} className="p-8 shadow-md rounded-xl bg-gray-50 border border-gray-300">
                <CardContent className="flex flex-col gap-3">
                  <h3 className="text-2xl font-bold text-black">{job.title}</h3>
                  <p className="text-lg text-gray-700 font-semibold">{job.company}</p>
                  <p className="text-md text-gray-500 italic">
                    {new Date(job.startDate).toLocaleDateString()} - {job.endDate ? new Date(job.endDate).toLocaleDateString() : "Present"}
                  </p>
                  <p className="mt-3 text-gray-800 leading-relaxed border-t pt-3">{job.responsibilities}</p>
                  <div className="mt-4 flex justify-end">
                    <Button className="bg-blue-600 text-white hover:bg-blue-700 shadow-md px-5 py-2 text-lg flex items-center">
                      <Pencil className="w-5 h-5 mr-2" /> Edit
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