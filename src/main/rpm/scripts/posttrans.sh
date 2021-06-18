#!/bin/bash
# for update from System-V
systemctl preset %{name}.service
systemctl start %{name}.service
exit 0