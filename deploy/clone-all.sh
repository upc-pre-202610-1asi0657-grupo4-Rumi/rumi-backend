#!/usr/bin/env bash
# Clona los 9 repositorios de Rumi al lado de rumi-backend. Ejecutar desde la carpeta que contendrá todos los repos.
set -e
ORG=https://github.com/upc-pre-202610-1asi0657-grupo4-Rumi
for r in rumi-api-gateway rumi-building-service rumi-monitoring-service rumi-seismic-service rumi-iam-service rumi-notification-service rumi-safety-service rumi-billing-service rumi-maintenance-service; do
  [ -d "$r" ] || git clone -b develop "$ORG/$r.git"
done
