# Confidence Scoring Algorithm

The score is clamped to 0-100. Initial auto-save threshold is 70.

| Signal | Weight |
|---|---:|
| Previously in vehicle | +20 |
| Trip speed exceeded 15 km/h | +10 |
| Speed below 2 km/h for 90 seconds | +15 |
| Vehicle-to-walking/on-foot/still transition | +20 |
| Known vehicle Bluetooth disconnected | +20 |
| CarPlay/Android Auto disconnect | +15 |
| User moved 20-100m away | +10 |
| Location stable | +5 |
| Valid stopping area | +5 |
| Stop under 45 seconds | -25 |
| Driving resumed | -50 |
| Poor GPS accuracy | -15 |
| Major road or junction | -15 |
| Poor GPS fallback available | +8 |

Detection reasons are stored as a set. Multiple reasons add `COMBINED_SENSOR_FUSION`.
