"use client";

import { Background } from "@/components/ui/background";
import { Card, CardContent } from "@/components/ui/card";
import { Label } from "@/components/ui/label";
import { useAuth } from "@/hooks/auth";
import { Button } from "@/components/ui/button";
import { Pencil, Sun, Moon, Monitor, Save, X } from "lucide-react";
import { useEffect, useState } from "react";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { useToast } from "@/contexts/ToastProvider";
import { useTheme } from "next-themes";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import SkillsCard from "@/components/SkillsCard";

export default function ProfilePage() {
  const { user } = useAuth();
  const { showSuccess, showError } = useToast();
  const { setTheme } = useTheme();
  const [isEditing, setIsEditing] = useState(false);
  const [phone, setPhone] = useState("");
  const [address, setAddress] = useState("");
  const [themePreference, setThemePreference] = useState("");

  const validatePhone = (phone: string) => {
    // E.164 format: +[country code][subscriber number]
    const phoneRegex = /^\+?[1-9]\d{9}$/;
    if (phone && !phoneRegex.test(phone)) {
      showError("Please enter a valid phone number (e.g., 1234567890)");
      return false;
    }
    return true;
  };

  async function fetchUser() {
    const res = await fetch("http://localhost:8080/api/profile", {
      credentials: "include",
    });
    if (!res.ok) throw new Error("Failed to fetch user");
    const data = await res.json();
    setPhone(data.phone || "");
    setAddress(data.address || "");
    setThemePreference(data.themePreference || "");
  }

  useEffect(() => {
    fetchUser();
  }, [user?.id]);

  const handleEdit = () => {
    setIsEditing(!isEditing);
  }

  const handleSave = async () => {
    if (!validatePhone(phone)) {
      return;
    }

    try {
      const res = await fetch("http://localhost:8080/api/profile", {
        method: "PUT",
        credentials: "include",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          phone,
          address,
          themePreference,
        }),
      });

      if (!res.ok) {
        const error = await res.json();
        throw new Error(error.message || "Failed to update profile");
      }
      
      showSuccess("Profile updated successfully");
      setIsEditing(false);
    } catch (err) {
      showError(err instanceof Error ? err.message : "Failed to update profile");
    }
  };

  const handleCancel = () => {
    fetchUser();
    setIsEditing(false);
  };

  const handleThemeChange = async (theme: string) => {
    try {
      const res = await fetch("http://localhost:8080/api/profile", {
        method: "PUT",
        credentials: "include",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          phone,
          address,
          themePreference: theme,
        }),
      });

      if (!res.ok) throw new Error("Failed to update theme");
      
      setThemePreference(theme);
      setTheme(theme);
    } catch (err) {
      showError("Failed to update theme");
    }
  };

  return (
    <Background className="relative flex flex-col items-center justify-start min-h-screen p-8 text-center">
      <div className="w-full max-w-4xl space-y-8">
        {/* Header Section */}
        <div className="flex flex-col items-center justify-center gap-2">
            <Avatar className="size-16">
                <AvatarImage src={user?.profilePictureUrl || `https://api.dicebear.com/9.x/initials/svg?seed=${user?.firstName}${user?.lastName}&backgroundType=gradientLinear`} />
                <AvatarFallback>{user?.firstName[0]}</AvatarFallback>
            </Avatar>
          <h1 className="text-4xl font-bold text-primary mb-4 drop-shadow-md">
            {user?.firstName} {user?.lastName}
          </h1>
        </div>

        {/* Contact Information Card */}
        <Card className="bg-white shadow-xl rounded-2xl p-8 border border-gray-200 dark:bg-neutral-900 dark:border-neutral-800">
          <CardContent className="space-y-6">
            <div className="relative">
              <h2 className="text-2xl font-bold text-primary text-center">Contact Information</h2>
              {!isEditing && (
                <Button
                  onClick={handleEdit}
                  className="bg-blue-600 text-white hover:bg-blue-700 absolute right-0 top-0"
                >
                  <Pencil size={16} />
                </Button>
              )}
            </div>
            <div className="space-y-4">
              <div className="flex flex-col items-center">
                <Label className="text-sm text-gray-500 dark:text-gray-400">Email</Label>
                <p className="text-lg font-medium">{user?.email}</p>
              </div>
              <div className="flex flex-col items-center">
                <Label className="text-sm text-gray-500 dark:text-gray-400">Phone</Label>
                {isEditing ? (
                  <div className="flex flex-col items-center">
                    <Input
                      value={phone}
                      onChange={(e) => {
                        setPhone(e.target.value);
                      }}
                      className="w-64"
                      placeholder="+1234567890"
                    />
                  </div>
                ) : (
                  <p className="text-lg font-medium">{phone || "Not provided"}</p>
                )}
              </div>
              <div className="flex flex-col items-center">
                <Label className="text-sm text-gray-500 dark:text-gray-400">Address</Label>
                {isEditing ? (
                  <Textarea
                    value={address}
                    onChange={(e) => setAddress(e.target.value)}
                    className="w-64"
                  />
                ) : (
                  <p className="text-lg font-medium">{address || "Not provided"}</p>
                )}
              </div>
            </div>
            {isEditing && (
              <div className="flex justify-center gap-4 mt-4">
                <Button
                  onClick={handleSave}
                  className="bg-green-600 text-white hover:bg-green-700"
                >
                  <Save className="mr-2 h-4 w-4" />
                  Save
                </Button>
                <Button
                  onClick={handleCancel}
                  className="bg-red-600 text-white hover:bg-red-700"
                >
                  <X className="mr-2 h-4 w-4" />
                  Cancel
                </Button>
              </div>
            )}
          </CardContent>
        </Card>

        <Card className="bg-white shadow-xl rounded-2xl py-8 border border-gray-200 dark:bg-neutral-900 dark:border-neutral-800">
          <h2 className="text-2xl font-bold text-primary text-center">Skills</h2>
          <CardContent>
            <SkillsCard />
          </CardContent>
        </Card>

        {/* Preferences Section */}
        <Card className="bg-white shadow-xl rounded-2xl p-8 border border-gray-200 dark:bg-neutral-900 dark:border-neutral-800">
          <CardContent>
            <h2 className="text-2xl font-bold text-primary mb-6">Preferences</h2>
            <div className="space-y-4">
              <div className="flex flex-col items-center space-y-4">
                <Label className="text-lg">Theme</Label>
                <div className="flex gap-4">
                  <Button 
                    variant={themePreference === "light" ? "default" : "outline"}
                    size="lg"
                    className="flex items-center gap-2"
                    onClick={() => handleThemeChange("light")}
                  >
                    <Sun className="h-4 w-4" />
                    Light
                  </Button>
                  <Button 
                    variant={themePreference === "dark" ? "default" : "outline"}
                    size="lg"
                    className="flex items-center gap-2"
                    onClick={() => handleThemeChange("dark")}
                  >
                    <Moon className="h-4 w-4" />
                    Dark
                  </Button>
                  <Button 
                    variant={themePreference === "system" ? "default" : "outline"}
                    size="lg"
                    className="flex items-center gap-2"
                    onClick={() => handleThemeChange("system")}
                  >
                    <Monitor className="h-4 w-4" />
                    System
                  </Button>
                </div>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </Background>
  );
}

