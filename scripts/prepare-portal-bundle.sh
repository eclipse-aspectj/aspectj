#!/bin/bash
# Script to prepare Maven Central Portal upload bundle
# This script orchestrates Maven commands to build, sign, generate checksums, and create the portal bundle

set -e

# Ensure we're running from the project root (where pom.xml is located)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
if [ ! -f "$PROJECT_ROOT/pom.xml" ]; then
  echo -e "${RED}Error: pom.xml not found. Please run this script from the project root.${NC}"
  exit 1
fi
cd "$PROJECT_ROOT"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Default values
DRY_RUN=false
SKIP_TESTS=false

# Parse command line arguments
while [[ $# -gt 0 ]]; do
  case $1 in
    --dry-run)
      DRY_RUN=true
      shift
      ;;
    --skip-tests)
      SKIP_TESTS=true
      shift
      ;;
    -h|--help)
      echo "Usage: $0 [OPTIONS]"
      echo ""
      echo "Options:"
      echo "  --dry-run      Skip GPG signing (simulate publish without signatures)"
      echo "  --skip-tests   Skip running tests"
      echo "  -h, --help     Show this help message"
      echo ""
      echo "This script prepares a bundle for Maven Central Portal upload by:"
      echo "  1. Building all artifacts (mvn package or verify)"
      echo "  2. Generating checksums (MD5, SHA1)"
      echo "  3. Creating GPG signatures (unless --dry-run)"
      echo "  4. Packaging into Maven Repository Layout archive"
      exit 0
      ;;
    *)
      echo "Unknown option: $1"
      echo "Use --help for usage information"
      exit 1
      ;;
  esac
done

# Get project version from POM (for display and finding bundle file only)
# Note: All Maven commands use the version directly from pom.xml via ${project.version}
VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
if [ -z "$VERSION" ]; then
  echo -e "${RED}Error: Could not determine project version from pom.xml${NC}"
  exit 1
fi

echo -e "${GREEN}Preparing portal bundle for version: ${VERSION} (from pom.xml)${NC}"
if [ "$DRY_RUN" = true ]; then
  echo -e "${YELLOW}DRY RUN MODE: GPG signing will be skipped${NC}"
fi
echo ""

# Build artifacts
# Note: We explicitly skip deploy phase since releases are uploaded via Central Portal, not Maven deploy
echo -e "${GREEN}[1/4] Building artifacts...${NC}"
if [ "$DRY_RUN" = true ]; then
  if [ "$SKIP_TESTS" = true ]; then
    mvn clean package -DskipTests "-Dmaven.deploy.skip=true"
  else
    mvn clean package "-Dmaven.deploy.skip=true"
  fi
else
  if [ "$SKIP_TESTS" = true ]; then
    mvn clean verify -P release -DskipTests "-Dmaven.deploy.skip=true"
  else
    mvn clean verify -P release "-Dmaven.deploy.skip=true"
  fi
fi

# Checksums are generated automatically by checksum-maven-plugin during package phase
echo -e "${GREEN}[2/4] Checksums generated during package phase${NC}"

# GPG signatures are created during verify phase (if not dry-run)
if [ "$DRY_RUN" = false ]; then
  echo -e "${GREEN}[3/4] GPG signatures created during verify phase${NC}"
else
  echo -e "${YELLOW}[3/4] Skipping GPG signatures (dry-run mode)${NC}"
fi

# Rename POM signature files if they exist (for production builds with GPG signing)
# This allows the assembly descriptor to reference them with the correct names
echo -e "${GREEN}[4/5] Preparing signature files for bundle...${NC}"
for module in aspectjrt aspectjweaver aspectjtools aspectjmatcher; do
  SIG_SOURCE="${module}/target/flattened-pom.xml.asc"
  SIG_TARGET="${module}/target/${module}-${VERSION}.pom.asc"
  if [ -f "$SIG_SOURCE" ]; then
    cp "$SIG_SOURCE" "$SIG_TARGET"
    echo "  Renamed ${module} POM signature"
  fi
done

# Create portal bundle (must run from root POM only, not submodules)
echo -e "${GREEN}[5/5] Creating portal bundle archive...${NC}"
mvn -f pom.xml -N assembly:single -Ddescriptor=portal-bundle

# Find the created bundle
BUNDLE_ZIP=$(find . -name "portal-bundle-${VERSION}.zip" -type f | head -1)
BUNDLE_TAR_GZ=$(find . -name "portal-bundle-${VERSION}.tar.gz" -type f | head -1)

echo ""
echo -e "${GREEN}✓ Portal bundle created successfully!${NC}"
echo ""
if [ -n "$BUNDLE_ZIP" ]; then
  echo -e "  ZIP archive: ${GREEN}${BUNDLE_ZIP}${NC}"
  ls -lh "$BUNDLE_ZIP" | awk '{print "  Size: " $5}'
fi
if [ -n "$BUNDLE_TAR_GZ" ]; then
  echo -e "  TAR.GZ archive: ${GREEN}${BUNDLE_TAR_GZ}${NC}"
  ls -lh "$BUNDLE_TAR_GZ" | awk '{print "  Size: " $5}'
fi
echo ""
echo "Next steps:"
if [ -n "$BUNDLE_ZIP" ]; then
  echo "  1. Extract and verify the bundle structure:"
  echo "     unzip -l $BUNDLE_ZIP | grep 'org/aspectj'"
  echo "     # Or extract and inspect:"
  echo "     unzip -q $BUNDLE_ZIP -d /tmp/aspectj-bundle-verify && find /tmp/aspectj-bundle-verify -type f | sort"
elif [ -n "$BUNDLE_TAR_GZ" ]; then
  echo "  1. Extract and verify the bundle structure:"
  echo "     tar -tzf $BUNDLE_TAR_GZ | grep 'org/aspectj'"
  echo "     # Or extract and inspect:"
  echo "     tar -xzf $BUNDLE_TAR_GZ -C /tmp/aspectj-bundle-verify && find /tmp/aspectj-bundle-verify -type f | sort"
fi
echo "  2. Verify all required files are present (JARs, POMs, checksums, signatures)"
if [ "$DRY_RUN" = true ]; then
  echo ""
  echo -e "${YELLOW}⚠️  IMPORTANT: This is a DRY-RUN bundle WITHOUT GPG signatures.${NC}"
  echo -e "${YELLOW}   This bundle CANNOT be uploaded to Maven Central.${NC}"
  echo -e "${YELLOW}   It is for local verification only (install to ~/.m2/repository and test).${NC}"
  echo ""
  echo -e "${GREEN}To create a production bundle for upload:${NC}"
  echo -e "${GREEN}  Run: ./scripts/prepare-portal-bundle.sh${NC}"
  echo -e "${GREEN}  (without --dry-run flag)${NC}"
else
  echo "  3. Upload the bundle to Maven Central Portal:"
  echo "     https://central.sonatype.com/publish/publish-portal-upload/"
fi

