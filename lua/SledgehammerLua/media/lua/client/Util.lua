----------------------------------------------------------------
-- Utils.lua
-- A Utilities class for Sledgehammer's Lua Framework.
--
-- @module Core
-- @author Jab
-- @license LGPL
----------------------------------------------------------------
Utils = class(function(o)

end);

function getFont(str, fallback)
	if not fallback then fallback = UIFont.Small; end
	local f = string.lower(str);
	local font = fallback;
	if f == "small" then
		font = UIFont.Small;
	elseif f == "medium" then
		font = UIFont.Medium;
	elseif f == "large" then
		font = UIFont.Large;
	elseif f == "massive" then
		font = UIFont.Massive;
	elseif f == "mainmenu1" then
		font = UIFont.MainMenu1;
	elseif f == "mainmenu2" then
		font = UIFont.MainMenu2;
	elseif f == "cred1" then
		font = UIFont.Cred1;
	elseif f == "cred2" then
		font = UIFont.Cred2;
	elseif f == "newsmall" then
		font = UIFont.NewSmall;
	elseif f == "newmedium" then
		font = UIFont.NewMedium;
	elseif f == "newlarge" then
		font = UIFont.NewLarge;
	elseif f == "code" then
		font = UIFont.Code;
	elseif f == "mediumnew" then
		font = UIFont.MediumNew;
	elseif f == "autonormsmall" then
		font = UIFont.AutoNormSmall;
	elseif f == "autonormmedium" then
		font = UIFont.AutoNormMedium;
	elseif f == "autonormlarge" then
		font = UIFont.AutoNormLarge;
	elseif f == "dialogue" then
		font = UIFont.Dialogue;
	elseif f == "intro" then
		font = UIFont.Intro;
	elseif f == "handwritten" then
		font = UIFont.Handwritten;
	end
	return font;
end

----------------------------------------------------------------
-- @return 	Returns a compressed table-array with no null entries.
----------------------------------------------------------------
function compress(t) 
	local o = {};
	local p = 0;
	for i, f in ipairs(t) do
		if f ~= nil then
			o[p] = f;
			p = p + 1;
		end
	end
	return o;
end

----------------------------------------------------------------
-- @return 	Returns a string with the first character upper-cased.
----------------------------------------------------------------
function firstToUpper(str)
    str = string.lower(str);
    return (str:gsub("^%l", string.upper))
end

----------------------------------------------------------------
-- @return 	Returns the module's name, without 'sledgehammer.module.'
-- leading it.
----------------------------------------------------------------
function toSimpleModuleName(mod)
	local splitMod = luautils.split(mod, ".");
	local modName = "";
	local length = tLength(splitMod);
	for index = 3, length, 1 do
		if modName == "" then
			modName = splitMod[index];
		else
			modName = modName.."."..splitMod[index]; 
		end
	end

	return modName;
end

-- https://gist.github.com/stuby/5445834#file-rprint-lua
--
--[[ rPrint(struct, [limit], [indent])   Recursively print arbitrary data. 
	Set limit (default 100) to stanch infinite loops.
	Indents tables as [KEY] VALUE, nested tables as [KEY] [KEY]...[KEY] VALUE
	Set indent ("") to prefix each line:    Mytable [KEY] [KEY]...[KEY] VALUE
--]]
function rPrint(s, l, i, o) -- recursive Print (structure, limit, indent)
	o = (o) or {};
	l = (l) or 1024; -- default item limit
	i = i or ""; -- indent string
	if (l<1) then print "ERROR: Item limit reached."; return l-1 end;
	local ts = type(s);
	if (ts ~= "table") then print (i,ts,s); return l-1 end
	print (i,ts);           -- print "table"
	for k,v in pairs(s) do  -- print "[KEY] VALUE"
		-- if o[k] ~= v then
		-- 	o[k] = v;
			if k ~= "parent" and k ~= "vscroll" and k ~= "children" then
				l = rPrint(v, l, i.."\t["..tostring(k).."]", o);
				if (l < 0) then 
					break; 
				end
			end
		-- end
	end
	return l
end	

----------------------------------------------------------------
-- @table T 	The table being examined.
-- @return  	Returns the length of the table.
----------------------------------------------------------------
function tLength(T)
	-- The counting variable for the table.
	local count = 0;
	-- Loop through all paired elements in the table.
	for _ in pairs(T) do 
		-- Discard the index, and increment the value.
		count = count + 1;
	end
	-- Return the result count.
	return count;
end

----------------------------------------------------------------
-- @table T 	The table being checked.
-- @object val 	The value being checked.
-- @return 		Returns whether or not the Table contains the given value.
----------------------------------------------------------------
function tContainsValue(T, val)
	for key, value in pairs(T) do
		if val == value then
			return true;
		end
	end
	return false;
end

----------------------------------------------------------------
-- @table T 	The table being checked.
-- @string nam 	The name being checked.
-- @return 		Returns whether or not the Table contains a value with the given name.
----------------------------------------------------------------
function tContainsKey(T, nam)
	for key, value in pairs(T) do
		if nam == key then
			return true;
		end
	end
	return false;
end

----------------------------------------------------------------
-- @string s 		The String being examined.
-- @UIFont font 	The font being used to draw the string.
-- @return 			The length of the String in pixels.
----------------------------------------------------------------
function sLength(s, font)
	return getTextManager():MeasureStringX(font, s);
end

----------------------------------------------------------------
-- @UIFont font 	The font being used to draw.
-- @return 			The height of the String in pixels.
----------------------------------------------------------------
function sHeight(font)
	if font == UIFont.Small then
	 return 16; --return getTextManager():MeasureStringY(font, s);
	end
	return 16;
end

----------------------------------------------------------------
-- @number val 	The value being limited.
-- @number min  The minimum value to be limited at.
-- @number max 	The maximum value to be limited at.
-- @return 		The value in limited form.
----------------------------------------------------------------
function limit(val, min, max)
	if val < min then val = min; end
	if val > max then val = max; end
	return val;
end

----------------------------------------------------------------
-- @return 	Returns the location of the code.
----------------------------------------------------------------
function getfunctionlocation()
  local w = debug.getinfo(2, "S")
  return w.short_src..":"..w.linedefined
end

debugLoadFunction = false;

----------------------------------------------------------------
-- @string body The body of the function.
-- @table  args The arguments to put in the function header. 
-- @return Returns a function from a body string and provided arguments.
----------------------------------------------------------------
function load_function(body, args) 
	local function_context = {};
	setmetatable(function_context, { __index = _G });
	local compiled = "return function(";
	local compiled_args = "";
	if args ~= nil and type(args) == "table" then
		for k,v in pairs(args) do
			if compiled_args == "" then
				compiled_args = v;
			else
				compiled_args = compiled_args..","..v;
			end
		end
	end
	compiled = compiled..compiled_args..") "..body.." end";
	-- Debug print function.
	if debugLoadFunction then print("compiled: "..compiled); end
	local f, err = loadstring(compiled, body);
  	if f then 
  		setfenv(f, function_context);
  		return f(); 
  	else 
  		return f, err; 
  	end
end

-- https://stackoverflow.com/a/7615129
function splitString(inputstr, sep)
    if sep == nil then
        sep = "%s";
    end
    local t={} ; i=1
    for str in string.gmatch(inputstr, "([^"..sep.."]+)") do
        t[i] = str;
        i = i + 1;
    end
    return t;
end

function contains(x, y, w, h, px, py) 
    return px >= x and px <= x + w and py >= y and py <= y + h;
end

function lerp(start, stop, percent)
    return (start + percent * (stop - start));
end

function ease_in_quad(value)
    return value * value;
end

function ease_out_quad(value)
    return value * (2 - value);
end