"use client";

import { useEffect, useState } from "react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardFooter } from "@/components/ui/card";
import { Background } from "@/components/ui/background";
import { Pencil, PlusIcon, Trash, Trash2 } from "lucide-react";
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
  responsibilities: string | string[]; // Can be either string (for editing) or string[] (for display)
  accomplishments: string | string[]; // Can be either string (for editing) or string[] (for display)
  location: string;
}

export default function CareerPage() {
  const { user } = useAuth();
  const { showError, showSuccess, showInfo } = useToast();
  const { activeProcesses, addActiveProcess } = useResumeProcessing();
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
    accomplishments: "",
    location: "",
  });

  // 2) Fetch career history whenever userId is available
  async function fetchCareerHistory() {
    if (!user?.id) return;
    try {
      console.log("Fetching career history for user:", user.id);
      const res = await fetch(`http://localhost:8080/api/resumes/career`, {
        credentials: "include",
      });
      if (!res.ok) throw new Error("Failed to fetch career history");
      const data = await res.json();
      console.log("Career history:", data.jobs);
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
  }, [activeProcesses]);

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
        accomplishments: "",
        location: "",
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
        accomplishments: "",
        location: "",
      });
    }
  };

  // Start "Edit" flow for a specific job
  const handleEdit = (index: number) => {
    setEditingIndex(index);
    const job = careerHistory[index];
    setFormData({
      ...job,
      responsibilities: Array.isArray(job.responsibilities) ? job.responsibilities.join("\n") : job.responsibilities,
      accomplishments: Array.isArray(job.accomplishments) ? job.accomplishments.join("\n") : job.accomplishments,
    });
  };

  // Handle changes in the form fields (input & textarea)
  const handleFormChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ) => {
    const { name, value } = e.target;
    if (name === "responsibilities" || name === "accomplishments") {
      // Store the raw text value for the textarea
      setFormData((prev) => ({
        ...prev,
        [name]: value,
      }));
    } else {
      setFormData((prev) => ({
        ...prev,
        [name]: value,
      }));
    }
  };

  // Handle save: CREATE (if adding new) or UPDATE (if editing)
  const handleSave = async () => {
    if (!user?.id) {
      showError("Not authenticated");
      return;
    }

    // Validation
    if (!formData.title || !formData.company || !formData.startDate) {
      showError("Please fill in job title, company, and start date");
      return;
    }

    // Process responsibilities and accomplishments into arrays before saving
    const processedData = {
      ...formData,
      responsibilities: (typeof formData.responsibilities === 'string' ? formData.responsibilities : formData.responsibilities.join("\n"))
        .split("\n")
        .filter((line: string) => line.trim() !== ""),
      accomplishments: (typeof formData.accomplishments === 'string' ? formData.accomplishments : formData.accomplishments.join("\n"))
        .split("\n")
        .filter((line: string) => line.trim() !== ""),
    };

    // Validate start date format
    const startDate = new Date(formData.startDate);
    if (isNaN(startDate.getTime())) {
      showError("Invalid start date format");
      return;
    }

    // Validate end date if provided
    if (formData.endDate) {
      const endDate = new Date(formData.endDate);
      if (isNaN(endDate.getTime())) {
        showError("Invalid end date format");
        return;
      }
      // Compare dates if both are provided
      if (startDate > endDate) {
        showError("Start date must be before end date");
        return;
      }
    }

    try {
      if (editingIndex === -1) {
        // Create new career record
        const res = await fetch(`http://localhost:8080/api/resumes/career`, {
          method: "POST",
          credentials: "include",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            ...processedData,
            endDate: formData.endDate || null
          }),
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
        const res = await fetch(`http://localhost:8080/api/resumes/career/${jobId}`, {
          method: "PUT",
          credentials: "include",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            ...processedData,
            endDate: formData.endDate || null
          }),
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
        accomplishments: "",
        location: "",
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
      const res = await fetch(`http://localhost:8080/api/resumes/career/${jobId}`, {
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

      const res = await fetch(`http://localhost:8080/api/resumes/career/freeform`, {
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
      const entryData = await res.json();
      addActiveProcess(entryData.statusId, 'freeform');
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
                  className="relative p-4 py-8 shadow-md rounded-xl bg-gray-50 border border-gray-300 dark:bg-neutral-800 dark:border-neutral-700"
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
                          name="location"
                          placeholder="Location"
                          value={formData.location}
                          onChange={handleFormChange}
                        />
                        <Input
                          className="dark:border-neutral-700"
                          name="startDate"
                          placeholder="Start Date (YYYY-MM-DD) *"
                          value={formData.startDate}
                          onChange={handleFormChange}
                          required
                        />
                        <Input
                          className="dark:border-neutral-700"
                          name="endDate"
                          placeholder="End Date (YYYY-MM-DD) or leave blank for Present"
                          value={formData.endDate}
                          onChange={handleFormChange}
                        />
                        <Textarea
                          className="dark:border-neutral-700"
                          name="responsibilities"
                          placeholder="Responsibilities (one per line)"
                          value={typeof formData.responsibilities === 'string' ? formData.responsibilities : formData.responsibilities.join("\n")}
                          onChange={handleFormChange}
                        />
                        <Textarea
                          className="dark:border-neutral-700"
                          name="accomplishments"
                          placeholder="Accomplishments (one per line)"
                          value={typeof formData.accomplishments === 'string' ? formData.accomplishments : formData.accomplishments.join("\n")}
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
                                accomplishments: "",
                                location: "",
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
                              <Button
                                variant="ghost"
                                size="icon"
                                className="absolute top-2 right-12 h-8 w-8 rounded-full bg-blue-100 hover:bg-blue-200 dark:bg-blue-900/20 dark:hover:bg-blue-900/40"
                                onClick={() => handleEdit(index)}
                              >
                                <Pencil className="h-4 w-4 text-blue-600 dark:text-bluw-400" />
                              </Button>
                              <Button
                                variant="ghost"
                                size="icon"
                                className="absolute top-2 right-2 h-8 w-8 rounded-full bg-red-100 hover:bg-red-200 dark:bg-red-900/20 dark:hover:bg-red-900/40"
                                onClick={() => handleDelete(index)}
                              >
                                <Trash2 className="h-4 w-4 text-red-600 dark:text-red-400" />
                              </Button>
                            <div className="w-full text-center">
                              <h3 className="text-2xl font-bold text-primary">
                                {job.title}
                              </h3>
                            </div>
                          <p className="text-lg text-gray-700 dark:text-white font-semibold">
                            {job.company}
                          </p>
                          <p className="text-md text-gray-500 dark:text-muted-foreground italic mb-2">
                            {job.location}
                          </p>
                          <p className="text-sm text-gray-500 dark:text-gray-400 mb-4">
                            {job.startDate} - {job.endDate || "Present"}
                          </p>
                          {Array.isArray(job.responsibilities) && job.responsibilities.length > 0 && (
                            <div className="mb-4">
                              <h4 className="text-lg font-semibold text-gray-700 dark:text-white mb-2">Responsibilities:</h4>
                              <ul className="list-disc list-inside text-gray-700 dark:text-white leading-relaxed">
                                {job.responsibilities.map((resp, i) => (
                                  <li key={i}>{resp}</li>
                                ))}
                              </ul>
                            </div>
                          )}
                          {Array.isArray(job.accomplishments) && job.accomplishments.length > 0 && (
                            <div>
                              <h4 className="text-lg font-semibold text-gray-700 dark:text-white mb-2">Accomplishments:</h4>
                              <ul className="list-disc list-inside text-gray-700 dark:text-white leading-relaxed">
                                {job.accomplishments.map((acc, i) => (
                                  <li key={i}>{acc}</li>
                                ))}
                              </ul>
                            </div>
                          )}
                        </div>
                      </>
                    )}
                  </CardContent>
                  <CardFooter className="-mt-4">
                      <div className="flex justify-between ml-auto">
                          {job.id && (
                              <p className="text-muted-foreground text-xs">
                                  CareerID: {job.id}
                              </p>
                          )}
                      </div>
                  </CardFooter>
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
                  name="location"
                  placeholder="Location"
                  value={formData.location}
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
                  onChange={handleFormChange}
                  className="min-h-[200px]"
                />
                <Textarea
                  name="accomplishments"
                  placeholder="Enter your accomplishments"
                  value={formData.accomplishments}
                  onChange={handleFormChange}
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
                  accomplishments: "",
                  location: "",
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
