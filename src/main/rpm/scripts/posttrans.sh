#!/bin/bash
# for update from System-V
systemctl preset rock.service
systemctl start rock.service
exit 0