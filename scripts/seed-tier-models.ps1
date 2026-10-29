# Original M20 authoring seeds. Embedded Blockbench sources are authoritative after editing.
$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
$utf8 = New-Object Text.UTF8Encoding($false)
foreach ($id in @('heavy_soldier', 'elite_soldier')) {
    $model = Get-Content (Join-Path $root 'art/blockbench/army_rifleman.bbmodel') -Raw | ConvertFrom-Json
    $model.name = $id
    $model.model_identifier = "standandhold.$id"
    $helmet = $model.elements | Where-Object name -eq 'helmet'
    $vest = $model.elements | Where-Object name -eq 'protective_vest'
    if (!$helmet -or !$vest) { throw 'Expected original helmet/vest seed geometry.' }
    if ($id -eq 'heavy_soldier') {
        $helmet.from = @(-4.5,29,-4.5); $helmet.to = @(4.5,34,4.5)
        $vest.from = @(-5,13,-3); $vest.to = @(5,23,3)
    } else {
        $helmet.from = @(-4.5,30,-4.5); $helmet.to = @(4.5,33,4.5)
        $vest.from = @(-4.5,15,-2.5); $vest.to = @(4.5,23,2.5)
    }
    function AddCube($name, $group, $from, $to, $uv, $suffix) {
        $cube = $model.elements[0] | ConvertTo-Json -Depth 20 | ConvertFrom-Json
        $cube.name = $name; $cube.uuid = "22000000-0000-4000-8000-0000000000$suffix"
        $cube.from = $from; $cube.to = $to; $cube.uv_offset = $uv
        # Explicit box-UV faces, matching the deterministic Java exporter.
        $w=$to[0]-$from[0]; $h=$to[1]-$from[1]; $d=$to[2]-$from[2]; $u=$uv[0]; $v=$uv[1]
        $cube.faces.east.uv=@($u,($v+$d),($u+$d),($v+$d+$h))
        $cube.faces.north.uv=@(($u+$d),($v+$d),($u+$d+$w),($v+$d+$h))
        $cube.faces.west.uv=@(($u+$d+$w),($v+$d),($u+2*$d+$w),($v+$d+$h))
        $cube.faces.south.uv=@(($u+2*$d+$w),($v+$d),($u+2*($w+$d)),($v+$d+$h))
        $cube.faces.up.uv=@(($u+$d),$v,($u+$d+$w),($v+$d))
        $cube.faces.down.uv=@(($u+$d+$w),($v+$d),($u+$d+2*$w),$v)
        $model.elements += $cube
        ($model.outliner | Where-Object name -eq $group).children += $cube.uuid
    }
    if ($id -eq 'heavy_soldier') {
        AddCube 'right_shoulder' 'right_arm' @(-8.5,19,-2.5) @(-3.5,22,2.5) @(36,32) '21'
        AddCube 'left_shoulder' 'left_arm' @(3.5,19,-2.5) @(8.5,22,2.5) @(36,32) '22'
    } else {
        AddCube 'visor' 'head' @(-4.5,28,-5) @(4.5,30,-4) @(36,32) '31'
        AddCube 'compact_pack' 'body' @(-3,14,1.75) @(3,21,4.75) @(36,40) '32'
    }
    foreach ($cube in $model.elements) {
        $w=$cube.to[0]-$cube.from[0]; $h=$cube.to[1]-$cube.from[1]; $d=$cube.to[2]-$cube.from[2]
        $u=$cube.uv_offset[0]; $v=$cube.uv_offset[1]
        $cube.faces.east.uv=@($u,($v+$d),($u+$d),($v+$d+$h))
        $cube.faces.north.uv=@(($u+$d),($v+$d),($u+$d+$w),($v+$d+$h))
        $cube.faces.west.uv=@(($u+$d+$w),($v+$d),($u+2*$d+$w),($v+$d+$h))
        $cube.faces.south.uv=@(($u+2*$d+$w),($v+$d),($u+2*($w+$d)),($v+$d+$h))
        $cube.faces.up.uv=@(($u+$d),$v,($u+$d+$w),($v+$d))
        $cube.faces.down.uv=@(($u+$d+$w),($v+$d),($u+$d+2*$w),$v)
    }
    $bytes = [IO.File]::ReadAllBytes((Join-Path $root "src/main/resources/assets/standandhold/textures/entity/$id.png"))
    $model.textures[0].name = "$id.png"
    $model.textures[0].source = 'data:image/png;base64,' + [Convert]::ToBase64String($bytes)
    [IO.File]::WriteAllText((Join-Path $root "art/blockbench/$id.bbmodel"), ($model | ConvertTo-Json -Depth 40) + "`n", $utf8)
}
