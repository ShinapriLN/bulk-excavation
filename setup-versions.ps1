$ErrorActionPreference = "Stop"

$versions = @(
    @{
        Minecraft = "1.21"
        Yarn = "1.21+build.9"
        Fabric = "0.102.0+1.21"
        Cloth = "15.0.140"
        ModMenu = "11.0.3"
    },
    @{
        Minecraft = "1.21.1"
        Yarn = "1.21.1+build.3"
        Fabric = "0.116.17+1.21.1"
        Cloth = "15.0.140"
        ModMenu = "11.0.3"
    },
    @{
        Minecraft = "1.21.2"
        Yarn = "1.21.2+build.1"
        Fabric = "0.106.1+1.21.2"
        Cloth = "16.0.143"
        ModMenu = "12.0.1"
    },
    @{
        Minecraft = "1.21.3"
        Yarn = "1.21.3+build.2"
        Fabric = "0.114.1+1.21.3"
        Cloth = "16.0.143"
        ModMenu = "12.0.1"
    },
    @{
        Minecraft = "1.21.4"
        Yarn = "1.21.4+build.8"
        Fabric = "0.119.4+1.21.4"
        Cloth = "17.0.144"
        ModMenu = "13.0.3"
    },
    @{
        Minecraft = "1.21.5"
        Yarn = "1.21.5+build.1"
        Fabric = "0.128.2+1.21.5"
        Cloth = "18.0.145"
        ModMenu = "14.0.1"
    },
    @{
        Minecraft = "1.21.6"
        Yarn = "1.21.6+build.1"
        Fabric = "0.128.2+1.21.6"
        Cloth = "19.0.147"
        ModMenu = "15.0.2"
    },
    @{
        Minecraft = "1.21.7"
        Yarn = "1.21.7+build.8"
        Fabric = "0.129.0+1.21.7"
        Cloth = "19.0.147"
        ModMenu = "15.0.2"
    },
    @{
        Minecraft = "1.21.8"
        Yarn = "1.21.8+build.1"
        Fabric = "0.136.1+1.21.8"
        Cloth = "19.0.147"
        ModMenu = "15.0.2"
    },
    @{
        Minecraft = "1.21.9"
        Yarn = "1.21.9+build.1"
        Fabric = "0.134.1+1.21.9"
        Cloth = "20.0.149"
        ModMenu = "16.0.1"
    },
    @{
        Minecraft = "1.21.10"
        Yarn = "1.21.10+build.3"
        Fabric = "0.138.4+1.21.10"
        Cloth = "20.0.149"
        ModMenu = "16.0.1"
    },
    @{
        Minecraft = "1.21.11"
        Yarn = "1.21.11+build.6"
        Fabric = "0.141.6+1.21.11"
        Cloth = "21.11.153"
        ModMenu = "17.0.0"
    }
)

$utf8 = New-Object System.Text.UTF8Encoding($false)

foreach ($v in $versions) {
    $dir = Join-Path "versions" $v.Minecraft
    New-Item -ItemType Directory -Force -Path $dir | Out-Null

    $content = @"
minecraft_version=$($v.Minecraft)
yarn_mappings=$($v.Yarn)
fabric_version=$($v.Fabric)

cloth_config_version=$($v.Cloth)
modmenu_version=$($v.ModMenu)

minecraft_dependency=$($v.Minecraft)
"@

    $path = Join-Path $dir "gradle.properties"

    [System.IO.File]::WriteAllText(
        (Join-Path $PWD $path),
        $content.Trim() + "`n",
        $utf8
    )

    Write-Host "Created $path"
}

Write-Host ""
Write-Host "Done."