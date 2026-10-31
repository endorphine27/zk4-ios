#pragma once

#include <stdbool.h>

typedef void (*CineboxAppIconCompletion)(bool);

bool CineboxSupportsAlternateAppIcons(void);
bool CineboxIsCurrentAlternateAppIcon(const char *name);
void CineboxSetAlternateAppIconName(const char *name, CineboxAppIconCompletion completion);
