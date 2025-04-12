"use client"

import * as React from "react"
import {
  Briefcase,
  FileUser,
  GraduationCap,
} from "lucide-react"

import { NavMain } from "@/components/nav-main"
import {
  Sidebar,
  SidebarContent,
} from "@/components/ui/sidebar"

const data = {
  navMain: [
    {
      title: "Resume",
      url: "/upload-resume",
      icon: FileUser,
      isActive: true,
    },
    {
      title: "Career",
      url: "/career",
      icon: Briefcase,
    },
    {
      title: "Education",
      url: "/education",
      icon: GraduationCap,
    },
  ],
}

export function AppSidebar({ ...props }: React.ComponentProps<typeof Sidebar>) {
  return (
    <Sidebar collapsible="icon" className="top-14" {...props}>
      <SidebarContent>
        <NavMain items={data.navMain} />
      </SidebarContent>
    </Sidebar>
  )
}
