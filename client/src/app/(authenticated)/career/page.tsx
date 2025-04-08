"use client";

import { useEffect, useState } from "react";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Background } from "@/components/ui/background";
import { Pencil, PlusIcon, Trash } from "lucide-react";
import { useAuth } from "@/hooks/auth";
import { useToast } from "@/contexts/ToastProvider";
import { useResumeProcessing } from "@/contexts/ResumeProcessingProvider";
import LoadingScreen from "@/components/LoadingScreen";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Textarea } from "@/components/ui/textarea";
import { Input } from "@/components/ui/input";

interface Job {
  id?: number; // For existing records
  title: string;
  company: string;
  startDate: string;
  endDate: string;
  responsibilities: string;
}

export default function CareerPage() {
  const { user } = useAuth();
  const { showError, showSuccess, showInfo } = useToast();
  const { activeResumeId } = useResumeProcessing();
  const { activeCareerUserId, setActiveCareerUserId } = useResumeProcessing();
  const [isLoading, setIsLoading] = useState(true);
  const [activeTab, setActiveTab] = useState("freeform");
  const [freeform, setFreeform] = useState("");

  // Career history data
  const [careerHistory, setCareerHistory] = useState<Job[]>([]);

  /**
   * editingIndex indicates which item is being edited:
   *  -1 => adding new,
   *  >=0 => editing that index,
   *  null => neither adding nor editing.
   */
  const [editingIndex, setEditingIndex] = useState<number | null>(null);

  // The form data for either add or edit
  const [formData, setFormData] = useState<Job>({
    title: "",
    company: "",
    startDate: "",
    endDate: "",
    responsibilities: "",
  });

  // 2) Fetch career history whenever userId is available
  async function fetchCareerHistory() {
    if (!user?.id) return;
    try {
      console.log("Fetching career history for user:", user.id);
      const res = await fetch(`http://localhost:8080/api/users/${user.id}/career`, {
        credentials: "include",
      });
      if (!res.ok) throw new Error("Failed to fetch career history");
      const data = await res.json();
      setCareerHistory(data.jobs || []);
      setIsLoading(false);
    } catch (err) {
      if (err instanceof Error) showError(err.message);
      else showError("An unknown error occurred");
    }
  }

  useEffect(() => {
    fetchCareerHistory();
  }, [user?.id]);

  useEffect(() => {
    const onUpdate = async () => {
      await new Promise((resolve) => setTimeout(resolve, 1000));
      fetchCareerHistory();
    }
    onUpdate();
  },[activeResumeId, activeCareerUserId])

  // Start "Add New Career" flow
  const handleAdd = () => {
    if (editingIndex == null) {
      setEditingIndex(-1);
      setFormData({
        title: "",
        company: "",
        startDate: "",
        endDate: "",
        responsibilities: "",
      });
    }
    if (editingIndex !== null) {
      setEditingIndex(null);
      setFormData({
        title: "",
        company: "",
        startDate: "",
        endDate: "",
        responsibilities: "",
      });
    }
  };

  // Start "Edit" flow for a specific job
  const handleEdit = (index: number) => {
    setEditingIndex(index);
    setFormData(careerHistory[index]);
  };

  // Handle changes in the form fields (input & textarea)
  const handleFormChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  // Handle save: CREATE (if adding new) or UPDATE (if editing)
  const handleSave = async () => {
    if (!user?.id) {
      showError("Not authenticated");
      return;
    }

    // Validation
    if (!formData.title || !formData.company || !formData.startDate || !formData.endDate) {
      showError("Please fill in all fields");
      return;
    }

    const startDate = new Date(formData.startDate);
    const endDate = new Date(formData.endDate);

    if (isNaN(startDate.getTime()) || isNaN(endDate.getTime())) {
      showError("Invalid date format");
      return;
    }
    if (startDate > endDate) {
      showError("Start date must be before end date");
      return;
    }

    try {
      if (editingIndex === -1) {
        // Create new career record
        const res = await fetch(`http://localhost:8080/api/users/${user.id}/career`, {
          method: "POST",
          credentials: "include",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(formData),
        });
        if (!res.ok) {
          const errText = await res.text();
          throw new Error(errText || "Failed to create career record");
        }
        showSuccess("Career record created successfully");
      } else {
        // Ensure editingIndex is not null and is >= 0
        if (editingIndex === null || editingIndex < 0) {
          showError("Invalid index for update");
          return;
        }
        // Use a local var to ensure TypeScript knows this is a number
        const idx = editingIndex;
        const jobId = careerHistory[idx].id;
        if (!jobId) {
          showError("Missing job ID");
          return;
        }
        const res = await fetch(`http://localhost:8080/api/users/${user.id}/career/${jobId}`, {
          method: "PUT",
          credentials: "include",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(formData),
        });
        if (!res.ok) {
          const errText = await res.text();
          throw new Error(errText || "Failed to update career record");
        }
        showSuccess("Career record saved successfully");
      }
      // Refresh career history after saving
      fetchCareerHistory();
    } catch (err) {
      if (err instanceof Error) showError(err.message);
      else showError("An unknown error occurred");
    } finally {
      setEditingIndex(null);
      setFormData({
        title: "",
        company: "",
        startDate: "",
        endDate: "",
        responsibilities: "",
      });
    }
  };

  // Handle deletion of a career record
  const handleDelete = async (index: number) => {
    if (!user?.id) {
      showError("Not authenticated");
      return;
    }
    const jobId = careerHistory[index].id;
    if (!jobId) {
      showError("Invalid job ID");
      return;
    }
    try {
      const res = await fetch(`http://localhost:8080/api/users/${user.id}/career/${jobId}`, {
        method: "DELETE",
        credentials: "include",
      });
      if (!res.ok) {
        const errText = await res.text();
        throw new Error(errText || "Failed to delete career record");
      }
      showSuccess("Career record deleted successfully");
      // Refresh career history after deletion
      fetchCareerHistory();
    } catch (err) {
      if (err instanceof Error) showError(err.message);
      else showError("An unknown error occurred");
    }
  };

  // Handle freeform submission
  const handleFreeformSubmit = async () => {
    if (!user?.id) {
      showError("Not authenticated");
      return;
    }

    if (!freeform) {
      showError("Please enter your career information");
      return;
    }

    try {
      const data = {
        text: freeform
      }

      const res = await fetch(`http://localhost:8080/api/users/${user.id}/career/freeform`, {
        method: "POST",
        credentials: "include",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data),
      });

      if (!res.ok) {
        const errText = await res.text();
        throw new Error(errText || "Failed to create career record");
      }

      showInfo("Freeform entry submitted. Please wait while we process your entry.");
      setActiveCareerUserId(user.id);
      setFreeform("");
      setEditingIndex(null);
    } catch (err) {
      if (err instanceof Error) showError(err.message);
      else showError("An unknown error occurred");
    }
  };

  if (isLoading) {
    return (
      <LoadingScreen/>
    )
  }

  return (
    <Background className="relative flex flex-col items-center justify-start min-h-screen p-8 text-center">
      <h2 className="text-4xl font-bold text-primary mb-8 drop-shadow-md">
        Career History
      </h2>

      {/* List of Career Cards */}
      <div className="w-full max-w-5xl bg-white shadow-xl rounded-2xl p-10 border border-gray-200 mb-8 
      dark:bg-neutral-900 dark:border-neutral-800">
        {careerHistory.length === 0 ? (
          <p className="text-xl text-foreground drop-shadow-sm text-center">
            No career history available.
          </p>
        ) : (
          <div className="space-y-8">
            {careerHistory.map((job, index) => {
              const isEditing = editingIndex === index;
              return (
                <Card
                  key={job.id ?? index}
                  className="p-4 py-8 shadow-md rounded-xl bg-gray-50 border border-gray-300 dark:bg-neutral-800 dark:border-neutral-700"
                >
                  <CardContent className="flex flex-col gap-3">
                    {isEditing ? (
                      <>
                        <Input
                          className="dark:border-neutral-700"
                          name="title"
                          placeholder="Job Title"
                          value={formData.title}
                          onChange={handleFormChange}
                        />
                        <Input
                          className="dark:border-neutral-700"
                          name="company"
                          placeholder="Company"
                          value={formData.company}
                          onChange={handleFormChange}
                        />
                        <Input
                          className="dark:border-neutral-700"
                          name="startDate"
                          placeholder="Start Date (YYYY-MM-DD)"
                          value={formData.startDate}
                          onChange={handleFormChange}
                        />
                        <Input
                          className="dark:border-neutral-700"
                          name="endDate"
                          placeholder="End Date (YYYY-MM-DD)"
                          value={formData.endDate}
                          onChange={handleFormChange}
                        />
                        <Textarea
                          className="dark:border-neutral-700"
                          name="responsibilities"
                          placeholder="Responsibilities"
                          value={formData.responsibilities}
                          onChange={handleFormChange}
                        />
                        <div className="flex gap-3 mt-4">
                          <Button
                            onClick={handleSave}
                            className="bg-blue-600 text-white hover:bg-blue-700"
                          >
                            Save
                          </Button>
                          <Button
                            variant="secondary"
                            onClick={() => {
                              setEditingIndex(null);
                              setFormData({
                                title: "",
                                company: "",
                                startDate: "",
                                endDate: "",
                                responsibilities: "",
                              });
                            }}
                          >
                            Cancel
                          </Button>
                        </div>
                      </>
                    ) : (
                      <>
                        <div className="flex flex-col">
                          <div className="relative flex items-center">
                            <div className="absolute right-0">
                              <div className="flex gap-3">
                                <Button
                                  onClick={() => handleEdit(index)}
                                  className="bg-blue-600 text-white hover:bg-blue-700"
                                >
                              <Pencil size={16} />
                            </Button>
                            <Button
                              onClick={() => handleDelete(index)}
                              className="bg-red-600 text-white hover:bg-red-700"
                              >
                              <Trash size={16} />
                            </Button>
                              </div>
                            </div>
                            <div className="w-full text-center">
                              <h3 className="text-2xl font-bold text-primary">
                                {job.title}
                              </h3>
                            </div>
                          </div>
                          <p className="text-lg text-gray-700 dark:text-white font-semibold">
                            {job.company}
                          </p>
                          <p className="text-md text-gray-500 dark:text-muted-foreground italic mb-4">
                            {new Date(job.startDate).toLocaleDateString()} -{" "}
                            {job.endDate
                              ? new Date(job.endDate).toLocaleDateString()
                              : "Present"}
                          </p>
                          <p className="text-gray-700 dark:text-white leading-relaxed">
                            {job.responsibilities}
                          </p>
                        </div>
                      </>
                    )}
                  </CardContent>
                </Card>
              );
            })}
          </div>
        )}
      </div>

      {editingIndex == null && (
      <div className="mb-8">
        <Button onClick={handleAdd} className="bg-green-500 hover:bg-green-600 text-white">
          <PlusIcon style={{ scale: 1.35 }} />
        </Button>
      </div>
      )}

      {/* Inline form for adding a new career entry (if editingIndex is -1) */}
      {editingIndex === -1 && (
        <div className="w-full max-w-5xl bg-white shadow-xl rounded-2xl p-10 border border-gray-200 dark:bg-neutral-900 dark:border-neutral-700">
          <h3 className="text-2xl font-bold mb-4">New Career Entry</h3>
          
          <Tabs value={activeTab} onValueChange={setActiveTab}>
            <TabsList className="grid w-full grid-cols-2">
              <TabsTrigger value="freeform">Free-form</TabsTrigger>
              <TabsTrigger value="structured">Structured</TabsTrigger>
            </TabsList>
            <TabsContent value="freeform">
              <div className="flex flex-col gap-2">
                <Textarea
                  name="freeform"
                  placeholder="Enter your career entry in free-form text"
                  value={freeform}
                  onChange={(e: React.ChangeEvent<HTMLTextAreaElement>) => {
                    setFreeform(e.target.value);
                  }}
                  className="min-h-[200px]"
                />
              </div>
            </TabsContent>
            <TabsContent value="structured">
              <div className="flex flex-col gap-2">
                <Input
                  name="title"
                  placeholder="Job Title"
                  value={formData.title}
                  onChange={handleFormChange}
                />
                <Input
                  name="company"
                  placeholder="Company"
                  value={formData.company}
                  onChange={handleFormChange}
                />
                <Input
                  name="startDate"
                  placeholder="Start Date (YYYY-MM-DD)"
                  value={formData.startDate}
                  onChange={handleFormChange}
                />
                <Input
                  name="endDate"
                  placeholder="End Date (YYYY-MM-DD)"
                  value={formData.endDate}
                  onChange={handleFormChange}
                />
                <Textarea
                  name="responsibilities"
                  placeholder="Enter your responsibilities"
                  value={formData.responsibilities}
                  onChange={(e: React.ChangeEvent<HTMLTextAreaElement>) => {
                    const lines = e.target.value.split('\n');
                    const formattedLines = lines.map((line: string) => {
                      if (line.trim() && !line.startsWith('•')) {
                        return `• ${line.trim()}`;
                      }
                      return line;
                    });
                    setFormData(prev => ({
                      ...prev,
                      responsibilities: formattedLines.join('\n')
                    }));
                  }}
                  className="min-h-[200px]"
                />
              </div>
            </TabsContent>
          </Tabs>

          <div className="flex gap-3 mt-4">
            <Button 
              onClick={activeTab === "freeform" ? handleFreeformSubmit : handleSave} 
              className="bg-green-600 text-white hover:bg-green-700"
            >
              Submit
            </Button>
            <Button
              className="bg-red-500 text-white hover:bg-red-600"
              onClick={() => {
                setEditingIndex(null);
                setFormData({
                  title: "",
                  company: "",
                  startDate: "",
                  endDate: "",
                  responsibilities: "",
                });
                setFreeform("");
              }}
            >
              Cancel
            </Button>
          </div>
        </div>
      )}
    </Background>
  );
}
