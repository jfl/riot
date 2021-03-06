<#import "/spring.ftl" as spring />
<?xml version="1.0" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
		"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<title></title>
		<@riot.stylesheet href="style/group.css" />
		<@riot.stylesheet href="style/group-custom.css" />
		<@riot.script src="path.js" />
		<@riot.script src="prototype/prototype.js" />
		<@riot.script src="scriptaculous/effects.js" />
		<@riot.script src="effects.js" />
		<@riot.script src="riot-js/util.js" />
		<script type="text/javascript" language="JavaScript">
			updatePath('${group.id}', '${group.objectId?if_exists}');
		</script>
	</head>
	<body>
		<div id="body-wrapper">
			<div id="wrapper">
				<div id="editors" class="main">
					<div class="box-title"><span class="label">${group.title?if_exists}</span></div>
					<#list group.editors as ref>
						<a class="editor ${ref.styleClass?default('default')}" href="${riot.url(ref.editorUrl)}" <#if ref.targetWindow?exists> target="${ref.targetWindow}"</#if>>
							<span class="icon"<#if ref.icon?exists> style="background-image:url(${riot.resource("style/icons/editors/" + ref.icon + ".gif")})"</#if>></span>
							<span class="text">
								<div>
									<div class="label">${ref.label}</div>
									<div class="description">${ref.description?if_exists}</div>
								</div>
							</span>
						</a>
					</#list>
				</div>
			</div>
			<div class="extra">
				<@riot.controller "/status" />
			</div>
		</div>
	</body>
</html>