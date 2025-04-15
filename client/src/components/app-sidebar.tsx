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
  SidebarFooter,
} from "@/components/ui/sidebar"
import { StatusDialog } from "@/components/StatusDialog"

const data = {
  navMain: [
    {
      title: "Resume",
      url: "",
      icon: FileUser,
      isActive: true,
      items: [
        {
          title: "Upload",
          url: "/upload-resume",
        }
      ],
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
      <SidebarFooter>
        <StatusDialog />
      </SidebarFooter>
    </Sidebar>
  )
}
