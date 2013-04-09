<?xml version="1.0" encoding="UTF-8"?>
<!--
! MPL 2.0 HEADER START
!
! This Source Code Form is subject to the terms of the Mozilla Public
! License, v. 2.0. If a copy of the MPL was not distributed with this
! file, You can obtain one at http://mozilla.org/MPL/2.0/.
!
! If applicable, add the following below this MPL 2.0 HEADER, replacing
! the fields enclosed by brackets "[]" replaced with your own identifying
! information:
!     Portions Copyright [yyyy] [name of copyright owner]
!
! MPL 2.0 HEADER END
!
!     Copyright 2013 ForgeRock AS
!
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
 <xsl:output method="xml" />
 <!--
      JCite needs Java's tools.jar to compile, and tools.jar is a system
      dependency as described in the Maven FAQ online ... not a provided
      dependency. The system dependency ends up expressed in the POM with
      concrete paths and so is not portable, even to a later installation
      of Java on the same system. Here is an example:

      <dependency>
        <groupId>com.sun</groupId>
        <artifactId>tools</artifactId>
        <version>1.7.0_17</version>
        <scope>system</scope>
        <systemPath>/Library/Java/JavaVirtualMachines/jdk1.7.0_17.jdk/Contents/Home/jre/../lib/tools.jar</systemPath>
      </dependency>

      That dependency does not seem to be needed to run the JCite artifact,
      however. This stylesheet therefore edits the dependency-reduced-pom.xml
      produced by the shade plugin and installed as the POM for the artifact
      to remove this system dependency.

      This copies nearly everything except for system dependencies.
 -->
 <xsl:template match="node()|@*">

  <xsl:choose>

   <xsl:when test="child::*[text()='system']">
    <xsl:comment> Removed system dependency </xsl:comment>
   </xsl:when>

   <xsl:otherwise>
    <xsl:copy>
     <xsl:apply-templates select="node()|@*"/>
    </xsl:copy>
   </xsl:otherwise>

  </xsl:choose>

 </xsl:template>
</xsl:stylesheet>
