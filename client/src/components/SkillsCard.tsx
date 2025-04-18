import React, { useState, useEffect } from "react";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { X, Plus } from "lucide-react";
import { useToast } from "@/contexts/ToastProvider";

interface Skill {
  id: number;
  name: string;
}

export const SkillsCard: React.FC = () => {
  const [skills, setSkills] = useState<Skill[]>([]);
  const [showAddSkill, setShowAddSkill] = useState(false);
  const [newSkill, setNewSkill] = useState("");
  const { showInfo, showError, showSuccess } = useToast();

  // Fetch skills on component mount
  useEffect(() => {
    fetchSkills();
  }, []);

  const fetchSkills = async () => {
    try {
      const response = await fetch("http://localhost:8080/api/skills", {
        credentials: "include",
        method: "GET",
        headers: {
          "Content-Type": "application/json",
        },
      });
      const data = await response.json();
      setSkills(data);
    } catch (err) {
      console.error("Error fetching skills:", err);
    }
  };

  const handleAddSkill = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newSkill.trim()) {
      return;
    }

    if (skills.some((skill) => skill.name.toLowerCase() === newSkill.trim().toLowerCase())) {
      showInfo("Skill already exists");
      return;
    }

    try {
      const response = await fetch("http://localhost:8080/api/skills", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        credentials: "include",
        body: JSON.stringify({ name: newSkill.trim() }),
      });
      const data = await response.json();
      fetchSkills();
      setNewSkill("");
      showSuccess("Skill added successfully");
    } catch (err) {
      console.error("Error adding skill:", err);
      showError("Error adding skill");
    }
  };

  const handleDeleteSkill = async (skillId: number) => {
    console.log(skillId);
    try {
      const response = await fetch(
        `http://localhost:8080/api/skills/${skillId}`,
        {
          method: "DELETE",
          credentials: "include",
          headers: {
            "Content-Type": "application/json",
          },
        }
      );
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      fetchSkills();
      showSuccess("Skill deleted successfully");
    } catch (err) {
      console.error("Error deleting skill:", err);
      showError("Error deleting skill");
    }
  };

  document.addEventListener("keydown", (e) => {
    if (e.key === "Escape") {
      setShowAddSkill(false);
    }
  });

  return (
      <CardContent>
        <div className="flex flex-wrap gap-2">
          {skills.map((skill) => (
            <Badge
              key={skill.id}
              variant="secondary"
              className="pl-4 py-2 text-sm flex items-center gap-1 group select-none"
            >
              {skill.name}
              <Button
                className="ml-1 p-0 rounded bg-transparent hover:bg-red-100 dark:hover:bg-red-900 shadow-none w-4 h-6"
                onClick={(e) => {
                  e.stopPropagation();
                  handleDeleteSkill(skill.id);
                }}
              >
                <X className="h-3 w-3 text-red-500" />
              </Button>
            </Badge>
          ))}

          {showAddSkill ? (
            <form
              onSubmit={handleAddSkill}
              className="flex items-center gap-1 outline outline-1 outline-gray-300 dark:outline-neutral-800 px-2 py-1 rounded-lg dark:bg-muted-background shadow-none"
            >
              <Input
                autoFocus
                value={newSkill}
                onChange={(e) => setNewSkill(e.target.value)}
                className="h-6 w-24 text-sm px-2 py-0 border-none focus-visible:ring-0 focus-visible:outline-none dark:bg-muted-background shadow-none"
                placeholder="New skill"
              />
              <Button type="submit" size="icon" className="h-6 w-6 p-1">
                <Plus className="h-4 w-4" />
              </Button>
            </form>
          ) : (
            <Badge
              variant="outline"
              className="cursor-pointer px-3 py-1 text-sm flex items-center gap-1 hover:bg-muted"
              onClick={() => setShowAddSkill(true)}
            >
              <Plus className="h-3 w-3" />
              Add Skill
            </Badge>
          )}
        </div>
      </CardContent>
  );
};

export default SkillsCard;
