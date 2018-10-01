1. Download the Latest Listings File
2. Update the new file path in config file
3. Download the previous Catalog File if the skus are already moved to LIVE tab(For skuVsPhoneName Mapping)
4. Make sure the verifiedCatalogFiles config is pointing to right location
5. Run Batch 1 - which will create skuVsPhone mapping by reading the previous catalog files and missingSkuVsPhoneMapping.txt file
6. Check if there are any SKUs not having name in missingSkusFromCurrentInventoryFile.txt. If there is any Sku, then get the name and add that entry in missingSkuVsPhoneMapping.txt.
7. Run Batch 2 - which populates the uniqueMobileList.txt from Listings File
8. Copy the data from uniqueMobileList.txt to updatedMobileList.txt and update the status of each phone model. ****Make sure you add ;; to exclude other phones.
9. Run Batch 3
8. missingSingleButComboPresent.txt -- Check if there are any Mobiles whose models are missing in single listing but present in combo
9. Also check failedSkus.txt if there are any updates failed
10. Done