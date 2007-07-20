setlocal
set jcitepath=%~dp0
set jcitecp=
for %%l in (%jcitepath%..\build\*.jar) do set jcitecp=!jcitecp!;%%l
for %%l in (%jcitepath%..\lib\*.jar) do set jcitecp=!jcitecp!;%%l
java -cp %jcitecp% ch.arrenbrecht.jcite.JCite %*