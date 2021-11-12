v0.4.2 / 2021-11-14
===================

### Bugfixes

  * Fixed generation of HTML reports for classes located in a directory that does not match the package name (#31)
  * Removed implicit dependencies from `koverCollectReports` task (#53)
  * Fixed a crash in report generation if there are several test tasks in the module and one of them does not contain tests (#46)
  * Ignore empty private constructors of utility classes (#6)
