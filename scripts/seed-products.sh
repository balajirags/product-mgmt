#!/usr/bin/env bash
# seed-products.sh — Creates 10 demo products via the Product API.
# Usage: ./scripts/seed-products.sh [BASE_URL]
# Default BASE_URL: http://localhost:8080

set -euo pipefail

BASE_URL="${1:-http://localhost:8080}"
API="$BASE_URL/api/v1/products"

GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

create() {
  local label="$1"
  local body="$2"
  local http_code
  local json

  http_code=$(curl -s -o /tmp/seed_response.json -w "%{http_code}" -X POST "$API" \
    -H "Content-Type: application/json" \
    -d "$body")
  json=$(cat /tmp/seed_response.json)

  if [[ "$http_code" == "201" ]]; then
    local id
    id=$(echo "$json" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)
    echo -e "${GREEN}✅ Created${NC} $label  (id: $id)"
  elif [[ "$http_code" == "409" ]]; then
    echo -e "⏭  Skipped  $label  (handle already exists)"
  else
    echo -e "${RED}❌ Failed${NC}  $label  (HTTP $http_code)"
    echo "   $json"
  fi
}

echo ""
echo "Seeding 10 products → $API"
echo "─────────────────────────────────────────────────────────"

# 1 — Classic White T-Shirt (PUBLISHED, with options + variants)
create "Classic White T-Shirt" '{
  "title": "Classic White T-Shirt",
  "handle": "classic-white-t-shirt",
  "status": "PUBLISHED",
  "description": "An everyday essential. 100% organic cotton, pre-washed for softness.",
  "subtitle": "100% organic cotton",
  "weight": 0.18,
  "thumbnail": "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=400",
  "metadata": { "material": "100% Organic Cotton", "care": "Machine wash cold" },
  "images": [
    { "url": "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=800" },
    { "url": "https://images.unsplash.com/photo-1583743814966-8936f5b7be1a?w=800" }
  ],
  "options": [
    { "title": "Size", "values": ["XS", "S", "M", "L", "XL", "XXL"] }
  ],
  "variants": [
    { "title": "XS", "sku": "TSH-WHT-XS", "manageInventory": true, "allowBackorder": false, "optionValues": { "Size": "XS" } },
    { "title": "S",  "sku": "TSH-WHT-S",  "manageInventory": true, "allowBackorder": false, "optionValues": { "Size": "S"  } },
    { "title": "M",  "sku": "TSH-WHT-M",  "manageInventory": true, "allowBackorder": true,  "optionValues": { "Size": "M"  } },
    { "title": "L",  "sku": "TSH-WHT-L",  "manageInventory": true, "allowBackorder": true,  "optionValues": { "Size": "L"  } },
    { "title": "XL", "sku": "TSH-WHT-XL", "manageInventory": true, "allowBackorder": false, "optionValues": { "Size": "XL" } }
  ]
}'

# 2 — Running Sneaker (PUBLISHED, size + colour)
create "Pro Runner Sneaker" '{
  "title": "Pro Runner Sneaker",
  "handle": "pro-runner-sneaker",
  "status": "PUBLISHED",
  "description": "Lightweight performance sneaker with responsive foam cushioning.",
  "subtitle": "Lightweight performance",
  "weight": 0.42,
  "height": 13.0,
  "width": 9.5,
  "length": 31.0,
  "thumbnail": "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=400",
  "metadata": { "material": "Mesh upper, rubber sole", "drop": "8mm" },
  "images": [
    { "url": "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800" },
    { "url": "https://images.unsplash.com/photo-1608231387042-66d1773070a5?w=800" }
  ],
  "options": [
    { "title": "Size",   "values": ["38", "39", "40", "41", "42", "43", "44"] },
    { "title": "Colour", "values": ["White/Black", "Navy/White", "All Black"] }
  ],
  "variants": [
    { "title": "40 / White-Black",  "sku": "SNK-40-WB",  "barcode": "5901234123457", "weight": 0.42, "manageInventory": true, "optionValues": { "Size": "40", "Colour": "White/Black" } },
    { "title": "41 / White-Black",  "sku": "SNK-41-WB",  "barcode": "5901234123464", "weight": 0.44, "manageInventory": true, "optionValues": { "Size": "41", "Colour": "White/Black" } },
    { "title": "42 / Navy-White",   "sku": "SNK-42-NW",  "barcode": "5901234123471", "weight": 0.45, "manageInventory": true, "optionValues": { "Size": "42", "Colour": "Navy/White" } },
    { "title": "43 / All Black",    "sku": "SNK-43-AB",  "barcode": "5901234123488", "weight": 0.46, "manageInventory": true, "optionValues": { "Size": "43", "Colour": "All Black" } }
  ]
}'

# 3 — Ceramic Coffee Mug (PUBLISHED, no variants)
create "Ceramic Coffee Mug" '{
  "title": "Ceramic Coffee Mug",
  "handle": "ceramic-coffee-mug",
  "status": "PUBLISHED",
  "description": "Hand-thrown stoneware mug, microwave and dishwasher safe. 350ml capacity.",
  "subtitle": "Hand-thrown stoneware",
  "weight": 0.38,
  "thumbnail": "https://images.unsplash.com/photo-1514228742587-6b1558fcca3d?w=400",
  "metadata": { "capacity_ml": "350", "dishwasher_safe": "yes" },
  "images": [
    { "url": "https://images.unsplash.com/photo-1514228742587-6b1558fcca3d?w=800" }
  ]
}'

# 4 — Yoga Mat (PUBLISHED, colour option)
create "Premium Yoga Mat" '{
  "title": "Premium Yoga Mat",
  "handle": "premium-yoga-mat",
  "status": "PUBLISHED",
  "description": "6mm thick non-slip natural rubber mat. 183 × 61 cm.",
  "subtitle": "Natural rubber, non-slip",
  "weight": 1.8,
  "height": 0.6,
  "width": 61.0,
  "length": 183.0,
  "thumbnail": "https://images.unsplash.com/photo-1601925228560-b06a63e85ae5?w=400",
  "metadata": { "material": "Natural rubber", "thickness_mm": "6" },
  "images": [
    { "url": "https://images.unsplash.com/photo-1601925228560-b06a63e85ae5?w=800" }
  ],
  "options": [
    { "title": "Colour", "values": ["Slate Grey", "Forest Green", "Dusty Rose"] }
  ],
  "variants": [
    { "title": "Slate Grey",   "sku": "YMA-GRY", "manageInventory": true, "optionValues": { "Colour": "Slate Grey"   } },
    { "title": "Forest Green", "sku": "YMA-GRN", "manageInventory": true, "optionValues": { "Colour": "Forest Green" } },
    { "title": "Dusty Rose",   "sku": "YMA-RSE", "manageInventory": true, "optionValues": { "Colour": "Dusty Rose"   } }
  ]
}'

# 5 — Leather Wallet (PUBLISHED)
create "Slim Leather Wallet" '{
  "title": "Slim Leather Wallet",
  "handle": "slim-leather-wallet",
  "status": "PUBLISHED",
  "description": "Minimalist bifold wallet. Full-grain vegetable-tanned leather. 4 card slots.",
  "subtitle": "Full-grain veg-tan leather",
  "weight": 0.065,
  "thumbnail": "https://images.unsplash.com/photo-1627123424574-724758594e93?w=400",
  "metadata": { "slots": "4", "material": "Full-grain leather" },
  "images": [
    { "url": "https://images.unsplash.com/photo-1627123424574-724758594e93?w=800" }
  ],
  "options": [
    { "title": "Colour", "values": ["Tan", "Dark Brown", "Black"] }
  ],
  "variants": [
    { "title": "Tan",        "sku": "WAL-TAN", "manageInventory": false, "optionValues": { "Colour": "Tan"        } },
    { "title": "Dark Brown", "sku": "WAL-DBR", "manageInventory": false, "optionValues": { "Colour": "Dark Brown" } },
    { "title": "Black",      "sku": "WAL-BLK", "manageInventory": false, "optionValues": { "Colour": "Black"      } }
  ]
}'

# 6 — Desk Lamp (PROPOSED — awaiting approval)
create "Architect Desk Lamp" '{
  "title": "Architect Desk Lamp",
  "handle": "architect-desk-lamp",
  "status": "PROPOSED",
  "description": "Adjustable LED desk lamp with 5 colour temperatures. USB-C charging port.",
  "subtitle": "LED, 5 colour temperatures",
  "weight": 1.1,
  "height": 45.0,
  "width": 16.0,
  "length": 16.0,
  "thumbnail": "https://images.unsplash.com/photo-1507473885765-e6ed057f782c?w=400",
  "externalId": "DL-2024-ARCH",
  "metadata": { "wattage": "12W", "colour_temp_range": "2700K-6500K", "usb_c_output": "18W" },
  "images": [
    { "url": "https://images.unsplash.com/photo-1507473885765-e6ed057f782c?w=800" }
  ]
}'

# 7 — Mechanical Keyboard (DRAFT — in progress)
create "TKL Mechanical Keyboard" '{
  "title": "TKL Mechanical Keyboard",
  "handle": "tkl-mechanical-keyboard",
  "status": "DRAFT",
  "description": "80% tenkeyless layout. Hot-swappable switches. PBT double-shot keycaps.",
  "subtitle": "Hot-swap, PBT keycaps",
  "weight": 0.85,
  "thumbnail": "https://images.unsplash.com/photo-1618384887929-16ec33fab9ef?w=400",
  "metadata": { "layout": "TKL 80%", "connectivity": "USB-C, Bluetooth 5.0" },
  "images": [
    { "url": "https://images.unsplash.com/photo-1618384887929-16ec33fab9ef?w=800" }
  ],
  "options": [
    { "title": "Switch",  "values": ["Red (Linear)", "Brown (Tactile)", "Blue (Clicky)"] },
    { "title": "Colour",  "values": ["Space Grey", "Arctic White"] }
  ],
  "variants": [
    { "title": "Red / Space Grey",    "sku": "KBD-RED-GRY", "manageInventory": true, "optionValues": { "Switch": "Red (Linear)",    "Colour": "Space Grey"   } },
    { "title": "Brown / Space Grey",  "sku": "KBD-BRN-GRY", "manageInventory": true, "optionValues": { "Switch": "Brown (Tactile)", "Colour": "Space Grey"   } },
    { "title": "Blue / Arctic White", "sku": "KBD-BLU-WHT", "manageInventory": true, "optionValues": { "Switch": "Blue (Clicky)",   "Colour": "Arctic White" } }
  ]
}'

# 8 — Bamboo Cutting Board (PUBLISHED)
create "Bamboo Cutting Board" '{
  "title": "Bamboo Cutting Board",
  "handle": "bamboo-cutting-board",
  "status": "PUBLISHED",
  "description": "End-grain bamboo board with juice groove and rubber feet. 40 × 28 × 3 cm.",
  "subtitle": "End-grain with juice groove",
  "weight": 1.2,
  "height": 3.0,
  "width": 28.0,
  "length": 40.0,
  "thumbnail": "https://images.unsplash.com/photo-1585515320310-259814833e62?w=400",
  "metadata": { "material": "Moso bamboo", "dimensions_cm": "40x28x3" },
  "images": [
    { "url": "https://images.unsplash.com/photo-1585515320310-259814833e62?w=800" }
  ]
}'

# 9 — Wireless Earbuds (PUBLISHED, colour option)
create "True Wireless Earbuds" '{
  "title": "True Wireless Earbuds",
  "handle": "true-wireless-earbuds",
  "status": "PUBLISHED",
  "description": "ANC, 30h total battery life, IPX5 water resistance. Multipoint connection.",
  "subtitle": "ANC, 30h battery, IPX5",
  "weight": 0.052,
  "thumbnail": "https://images.unsplash.com/photo-1590658268037-6bf12165a8df?w=400",
  "externalId": "TWS-PRO-2025",
  "metadata": { "battery_total_h": "30", "anc": "yes", "water_resistance": "IPX5" },
  "images": [
    { "url": "https://images.unsplash.com/photo-1590658268037-6bf12165a8df?w=800" }
  ],
  "options": [
    { "title": "Colour", "values": ["Midnight Black", "Pearl White", "Sage Green"] }
  ],
  "variants": [
    { "title": "Midnight Black", "sku": "TWS-BLK", "barcode": "5901234200001", "manageInventory": true, "optionValues": { "Colour": "Midnight Black" } },
    { "title": "Pearl White",    "sku": "TWS-WHT", "barcode": "5901234200018", "manageInventory": true, "optionValues": { "Colour": "Pearl White"    } },
    { "title": "Sage Green",     "sku": "TWS-GRN", "barcode": "5901234200025", "manageInventory": true, "optionValues": { "Colour": "Sage Green"     } }
  ]
}'

# 10 — Scented Candle (REJECTED — discontinued)
create "Hand-Poured Soy Candle" '{
  "title": "Hand-Poured Soy Candle",
  "handle": "hand-poured-soy-candle",
  "status": "REJECTED",
  "description": "150g soy wax candle with cotton wick. 35–40 hour burn time.",
  "subtitle": "Soy wax, cotton wick",
  "weight": 0.22,
  "thumbnail": "https://images.unsplash.com/photo-1603006905003-be475563bc59?w=400",
  "metadata": { "burn_time_h": "35-40", "wax": "100% soy", "wick": "cotton" },
  "images": [
    { "url": "https://images.unsplash.com/photo-1603006905003-be475563bc59?w=800" }
  ],
  "options": [
    { "title": "Scent", "values": ["Cedarwood & Sage", "Vanilla & Sandalwood", "Sea Salt & Driftwood"] }
  ],
  "variants": [
    { "title": "Cedarwood & Sage",        "sku": "CND-CDR", "manageInventory": false, "optionValues": { "Scent": "Cedarwood & Sage"        } },
    { "title": "Vanilla & Sandalwood",    "sku": "CND-VAN", "manageInventory": false, "optionValues": { "Scent": "Vanilla & Sandalwood"    } },
    { "title": "Sea Salt & Driftwood",    "sku": "CND-SEA", "manageInventory": false, "optionValues": { "Scent": "Sea Salt & Driftwood"    } }
  ]
}'

echo "─────────────────────────────────────────────────────────"
echo "Done. Check the product list at http://localhost:8081"
echo ""
