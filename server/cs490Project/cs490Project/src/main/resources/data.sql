INSERT INTO resume_templates (template_id, name, description, format_type, preview_url, template_content)
VALUES (
    'classic',
    'Classic',
    'A clean and professional LaTeX resume template with a modern layout',
    'pdf',
    'https://i.imgur.com/abA9Nq4l.png',
    '\\documentclass[letterpaper,11pt]{article}

\\usepackage{latexsym}
\\usepackage[empty]{fullpage}
\\usepackage{titlesec}
\\usepackage{marvosym}
\\usepackage[usenames,dvipsnames]{color}
\\usepackage{verbatim}
\\usepackage{enumitem}
\\usepackage[hidelinks]{hyperref}
\\usepackage{fancyhdr}
\\usepackage[english]{babel}
\\usepackage{tabularx}
\\input{glyphtounicode}

\\pagestyle{fancy}
\\fancyhf{} % clear all header and footer fields
\\fancyfoot{}
\\renewcommand{\\headrulewidth}{0pt}
\\renewcommand{\\footrulewidth}{0pt}

% Adjust margins
\\addtolength{\\oddsidemargin}{-0.5in}
\\addtolength{\\evensidemargin}{-0.5in}
\\addtolength{\\textwidth}{1in}
\\addtolength{\\topmargin}{-.5in}
\\addtolength{\\textheight}{1.0in}

\\urlstyle{same}

\\raggedbottom
\\raggedright
\\setlength{\\tabcolsep}{0in}

% Sections formatting
\\titleformat{\\section}{
  \\vspace{-4pt}\\scshape\\raggedright\\large
}{}{0em}{}[\\color{black}\\titlerule \\vspace{-5pt}]

% Ensure that generate pdf is machine readable/ATS parsable
\\pdfgentounicode=1

%-------------------------
% Custom commands
\\newcommand{\\resumeItem}[1]{
  \\item\\small{
    {#1 \\vspace{-2pt}}
  }
}

\\newcommand{\\resumeSubheading}[4]{
  \\vspace{-2pt}\\item
    \\begin{tabular*}{0.97\\textwidth}[t]{l@{\\extracolsep{\\fill}}r}
      \\textbf{#1} & #2 \\\\
      \\textit{\\small#3} & \\textit{\\small #4} \\\\
    \\end{tabular*}\\vspace{-7pt}
}

\\newcommand{\\resumeSubSubheading}[2]{
    \\item
    \\begin{tabular*}{0.97\\textwidth}{l@{\\extracolsep{\\fill}}r}
      \\textit{\\small#1} & \\textit{\\small #2} \\\\
    \\end{tabular*}\\vspace{-7pt}
}

\\newcommand{\\resumeProjectHeading}[2]{
    \\item
    \\begin{tabular*}{0.97\\textwidth}{l@{\\extracolsep{\\fill}}r}
      \\small#1 & #2 \\\\
    \\end{tabular*}\\vspace{-7pt}
}

\\newcommand{\\resumeSubItem}[1]{\\resumeItem{#1}\\vspace{-4pt}}

\\renewcommand\\labelitemii{$\\vcenter{\\hbox{\\tiny$\\bullet$}}$}

\\newcommand{\\resumeSubHeadingListStart}{\\begin{itemize}[leftmargin=0.15in, label={}]}
\\newcommand{\\resumeSubHeadingListEnd}{\\end{itemize}}
\\newcommand{\\resumeItemListStart}{\\begin{itemize}}
\\newcommand{\\resumeItemListEnd}{\\end{itemize}\\vspace{-5pt}}

\\begin{document}

\\begin{center}
    \\textbf{\\Huge \\scshape {{NAME}}} \\\\ \\vspace{1pt}
    \\small {{PHONE}} $|$ \\href{mailto:{{EMAIL}}}{\\underline{{{EMAIL}}}} $|$ {{ADDRESS}}
\\end{center}

\\section{Education}
{{EDUCATION_SECTION}}

\\section{Experience}
{{EXPERIENCE_SECTION}}

\\section{Skills}
{{SKILLS_SECTION}}

\\end{document}'
);

INSERT INTO resume_templates (template_id, name, description, format_type, preview_url, template_content)
VALUES (
    'compact',
    'Compact',
    'A modern and compact LaTeX resume template with clean typography and efficient use of space',
    'pdf',
    'https://i.imgur.com/yIMarY0l.png',
    '\\documentclass[11pt]{article}

\\usepackage[T1]{fontenc}
\\usepackage{inter} 
\\renewcommand*\\familydefault{\\sfdefault}
\\usepackage[none]{hyphenat}
\\usepackage{geometry}
\\geometry{
    a4paper,
    top=1.8cm,
    bottom=1in,
    left=2.5cm,
    right=2.5cm
}

\\setcounter{secnumdepth}{0} % remove section numbering
\\pdfgentounicode=1 % make ATS friendly

\\usepackage{enumitem}
\\setlist[itemize]{
    noitemsep,
    left=2em
}
\\setlist[description]{itemsep=0pt}
\\setlist[enumerate]{align=left}
\\usepackage[dvipsnames]{xcolor}
\\colorlet{icnclr}{gray}
\\usepackage{titlesec}
\\titlespacing{\\subsection}{0pt}{*0}{*0}
\\titlespacing{\\subsubsection}{0pt}{*0}{*0}
\\titleformat{\\section}{\\color{Sepia}\\large\\fontseries{black}\\selectfont\\uppercase}{}{}{\\ruleafter}[\\global\\RemVStrue]
\\titleformat{\\subsection}{\\fontseries{bold}\\selectfont}{}{}{\\rvs}
\\titleformat{\\subsubsection}{\\color{gray}\\fontseries{bold}\\selectfont}{}{}{}

\\usepackage{xhfill} 
\\newcommand\\ruleafter[1]{#1~\\xrfill[.5ex]{1pt}[gray]} % add rule after title in .5 x-height 

\\newif\\ifRemVS % remove vspace between \\section & \\subsection
\\newcommand{\\rvs}{
    \\ifRemVS
        \\vspace{-1.5ex}
    \\fi
    \\global\\RemVSfalse
}

\\usepackage{fontawesome5}

\\usepackage[bookmarks=false]{hyperref} % [imp!]
\\hypersetup{ 
    colorlinks=true,
    urlcolor=Sepia,
    pdftitle={My Resume},
}

\\usepackage[page]{totalcount}
\\usepackage{fancyhdr}
\\pagestyle{fancy}
\\renewcommand{\\headrulewidth}{0pt}	
\\fancyhf{}							

\\usepackage{amsmath}
\\usepackage{amsfonts}

\\begin{document}

%== HEADER ==%
\\begin{center}      
    {\\fontsize{28}{28}\\selectfont {{NAME}}} \\\\ \\bigskip

    {\\color{icnclr}\\faMapMarker} {{ADDRESS}} \\quad 
    {\\color{icnclr}\\faEnvelope[regular]} \\href{mailto:{{EMAIL}}}{{{EMAIL}}} \\quad
    {\\color{icnclr}\\faIcon{mobile-alt}} \\href{tel:{{PHONE}}}{{{PHONE}}} 
\\end{center}

\\section{Education}
{{EDUCATION_SECTION}}

\\section{Experience}
{{EXPERIENCE_SECTION}}

\\section{Skills}
{{SKILLS_SECTION}}

\\end{document}'
);

INSERT INTO resume_templates (template_id, name, description, format_type, preview_url, template_content)
VALUES (
    'twocolumn',
    'Two Column',
    'A two-column LaTeX resume template with clean typography and efficient space usage',
    'pdf',
    'https://i.imgur.com/m9zpZSil.png',
    '\\documentclass[12pt]{article}
\\usepackage[english]{babel}
\\usepackage{cmbright}
\\usepackage{enumitem}
\\usepackage{fancyhdr}
\\usepackage{fontawesome5}
\\usepackage{geometry}
\\usepackage{hyperref}
\\usepackage[sf]{libertine}
\\usepackage{microtype}
\\usepackage{paracol}
\\usepackage{supertabular}
\\usepackage{titlesec}
\\hypersetup{colorlinks, urlcolor=black, linkcolor=black}

% Geometry
\\geometry{hmargin=1.75cm, vmargin=2.5cm}
\\columnratio{0.60, 0.40}
\\setlength\\columnsep{0.05\\textwidth}
\\setlength\\parindent{0pt}
\\setlength{\\smallskipamount}{8pt plus 3pt minus 3pt}
\\setlength{\\medskipamount}{16pt plus 6pt minus 6pt}
\\setlength{\\bigskipamount}{24pt plus 8pt minus 8pt}

% Style
\\pagestyle{empty}
\\titleformat{\\section}{\\scshape\\LARGE\\raggedright}{}{0em}{}[\\titlerule]
\\titlespacing{\\section}{0pt}{\\bigskipamount}{\\smallskipamount}
\\newcommand{\\heading}[2]{\\centering{\\sffamily\\Huge #1}\\\\\\smallskip{\\large{#2}}}
\\newcommand{\\entry}[4]{{{\\textbf{#1}}} \\hfill #3 \\\\ #2 \\hfill #4}
\\newcommand{\\tableentry}[3]{\\textsc{#1} & #2\\expandafter\\ifstrequal\\expandafter{#3}{}{\\\\}{\\\\[6pt]}}

\\begin{document}

\\vspace*{\\fill}

\\begin{paracol}{2}

% Name & headline
\\heading{{{NAME}}}{{{HEADLINE}}}

\\switchcolumn

% Identity card
\\vspace{0.01\\textheight}
\\begin{supertabular}{ll}
  \\footnotesize\\faPhone & {{PHONE}} \\\\
  \\footnotesize\\faEnvelope & \\href{mailto:{{EMAIL}}}{{{EMAIL}}} \\\\
  \\footnotesize\\faMapMarker & {{ADDRESS}} \\\\
\\end{supertabular}

\\bigskip
\\switchcolumn*

\\section{experience}
{{EXPERIENCE_SECTION}}

\\switchcolumn

\\section{education}
{{EDUCATION_SECTION}}

\\section{skills}
{{SKILLS_SECTION}}

\\end{paracol}

\\vspace*{\\fill}

\\end{document}'
);